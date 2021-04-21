package software.amazon.redshift.endpointauthorization;

import com.amazonaws.SdkClientException;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorization;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationsPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidAuthorizationStateException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        ResourceModel resourceModel = request.getDesiredResourceState();
        /*
            If the update request has a list of VPCs...
            - Get the existing auth
            - Determine existing VPCs
            - If there are any existing ones that are in the update request, ignore them
            - If there are any that do not exist in the update request, delete those
            - Add in the VPCs that are in the VPC id list

            If the update request has no vpc ids, then we authorize all like normal.
         */
        // This should do its own logic. The create handler will throw a resource already exists
        // exception when trying to call itself on something that already exists.
        // AWS-Redshift-EndpointAuthorization::Create
        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest((model) ->
                                        Translator.translateToCreateRequest(model))
                                .makeServiceCall(this::updateEndpointAuthorization)
                                .progress())
                .then(progress ->
                        getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }


    @VisibleForTesting
    AuthorizeEndpointAccessResponse updateEndpointAuthorization(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        AuthorizeEndpointAccessResponse response = null;

        // Validate the auth exists. If it does not, this throws the CfnNotFound error
        validateAuthExists(request, proxyClient);

        if (doesNotExist(request.account())) {
            throw new CfnInvalidRequestException(request.toString());
        }

        // Update request will include a list of VPC ids. After the request is handled, that should be the list of
        // vpc ids remaining on the auth.

        try {
            List<String> existingVpcIds = getExistingVpcIds(request, proxyClient);
            List<String> vpcIdsToSend = null;
            List<String> vpcIdsInUpdateRequest = request.vpcIds();

            // This means that we are trying to authorize specific after doing an authorize all - not allowed
            if (existingVpcIds.isEmpty()) {
                // If we had authorized all before, we cannot go from auth all -> auth specific yet
                if (!vpcIdsInUpdateRequest.isEmpty()) {
                    throw new CfnInvalidRequestException(request.toString());
                }
                // Otherwise, we are trying to do an idempotent update?
                // TODO handle allow all -> allow all
            }

            // First, send the remove auth call
            List<String> vpcIdsToRemove = getVpcIdsToRemove(existingVpcIds, vpcIdsInUpdateRequest);
            if (!vpcIdsToRemove.isEmpty()) {
                removeVpcIds(request, vpcIdsToRemove, proxyClient);
            }

            // Then, handle adding new VPC ids
            // For each of the existing VPC ids, remove it from the list of VPC ids to send as an auth request
            if (!existingVpcIds.isEmpty()) {
                vpcIdsToSend = getVpcIdsToAdd(existingVpcIds, vpcIdsInUpdateRequest);
            }

            AuthorizeEndpointAccessRequest newRequest = AuthorizeEndpointAccessRequest.builder()
                    .vpcIds(vpcIdsToSend)
                    .account(request.account())
                    .clusterIdentifier(request.clusterIdentifier())
                    .build();

            response = proxyClient.injectCredentialsAndInvokeV2(
                    newRequest, proxyClient.client()::authorizeEndpointAccess
            );
        } catch (EndpointAuthorizationAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ClusterNotFoundException | InvalidParameterValueException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(request.toString(), e);
        } catch (InvalidAuthorizationStateException e) {
            throw new CfnResourceConflictException(e);
        } catch (EndpointAuthorizationsPerClusterLimitExceededException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (RedshiftException | SdkClientException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }

    List<String> getExistingVpcIds(final AuthorizeEndpointAccessRequest request,
                                   final ProxyClient<RedshiftClient> proxyClient) {
        // Make a call to check the existing VPC ids that exist for the auth. Remove the ones that already
        // exist before making the API call, otherwise we get an error saying vpc id already exists.
        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(request.account())
                .clusterIdentifier(request.clusterIdentifier())
                .build();

        DescribeEndpointAuthorizationResponse describeResponse = null;


        describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                describeRequest, proxyClient.client()::describeEndpointAuthorization
        );

        List<EndpointAuthorization> endpointAuthorizationList = describeResponse.endpointAuthorizationList();
        if (endpointAuthorizationList.isEmpty()) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    String.format("account:%s-clusteridentifier:%s",
                            request.account(),
                            request.clusterIdentifier())
            );
        }

        return endpointAuthorizationList.get(0).allowedVPCs();
    }

    void removeVpcIds(AuthorizeEndpointAccessRequest request,
                      List<String> vpcIdsToRemove,
                      final ProxyClient<RedshiftClient> proxyClient) {

        RevokeEndpointAccessRequest revokeRequest = RevokeEndpointAccessRequest.builder()
                .clusterIdentifier(request.clusterIdentifier())
                .account(request.account())
                .vpcIds(vpcIdsToRemove)
                .build();

        proxyClient.injectCredentialsAndInvokeV2(revokeRequest, proxyClient.client()::revokeEndpointAccess);
    }

    List<String> getVpcIdsToAdd(List<String> existingVpcIds, List<String> vpcIdsInUpdateRequest) {
        // We are adding a new VPC id to the auth list of it is in the vpcIdsInUpdateRequest, but not in
        // the existingVpcIds list
        return vpcIdsInUpdateRequest.stream()
                .filter(vpcId -> !existingVpcIds.contains(vpcId))
                .collect(Collectors.toList());
    }

    List<String> getVpcIdsToRemove(List<String> existingVpcIds, List<String> vpcIdsInUpdateRequest) {
        // We are removing a VPC if if it is in the existingVpcIds list, but not in the vpcIdsInUpdateRequest
        return existingVpcIds.stream()
                .filter(vpcId -> !vpcIdsInUpdateRequest.contains(vpcId))
                .collect(Collectors.toList());
    }
}
