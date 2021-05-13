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
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
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

import java.util.Collections;
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
                                .translateToServiceRequest((model) -> Translator.translateToCreateRequest(model))
                                .makeServiceCall(this::authorizeEndpointAccess)
                                .progress())
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Delete",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest((model) -> Translator.translateToRevokeRequest(model))
                                .makeServiceCall(this::revokeEndpointAuthorization).progress())
                .then(progress ->
                        getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }


    @VisibleForTesting
    AuthorizeEndpointAccessResponse authorizeEndpointAccess(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        if (doesNotExist(request.account())) {
            logger.log("request did not contain an account");
            throw new CfnInvalidRequestException(request.toString());
        }

        // Update request will include a list of VPC ids. After the request is handled, that should be the list of
        // vpc ids remaining on the auth.
        try {
            List<String> existingVpcIds = getExistingVpcIds(
                    request.account(),
                    request.clusterIdentifier(),
                    proxyClient
            );
            List<String> vpcIdsInUpdateRequest = request.vpcIds();

            // This means that we are trying to authorize specific after doing an authorize all - not allowed
            if (existingVpcIds.isEmpty()) {
                // If we had authorized all before, we cannot go from auth all -> auth specific yet
                if (!vpcIdsInUpdateRequest.isEmpty()) {
                    throw new CfnInvalidRequestException(request.toString());
                }
                // Otherwise, we are trying to do an idempotent update
                return null;
            }

            // Then, handle adding new VPC ids
            // For each of the existing VPC ids, remove it from the list of VPC ids to send as an auth request
            // If we are doing an authorize all call, then we can just set the vpc ids to empty
            List<String> vpcIdsToSend = vpcIdsInUpdateRequest.isEmpty() ?
                    Collections.emptyList() :
                    getVpcIdsToAdd(existingVpcIds, vpcIdsInUpdateRequest);

            // If the request was an authorize-all request, then we should authorize all with the empty list.
            // If the request had vpcIds in it, then we should skip this.
            if (vpcIdsToSend.isEmpty() && !vpcIdsInUpdateRequest.isEmpty()) {
                // If we have no vpcs to add to the auth, but the request was not an empty one, then
                // we should not call the auth api with an empty list (this would result in an auth all)
                return null;
            }

            AuthorizeEndpointAccessRequest newRequest = AuthorizeEndpointAccessRequest.builder()
                    .vpcIds(vpcIdsToSend)
                    .account(request.account())
                    .clusterIdentifier(request.clusterIdentifier())
                    .build();

            return proxyClient.injectCredentialsAndInvokeV2(
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
    }

    @VisibleForTesting
    RevokeEndpointAccessResponse revokeEndpointAuthorization(
            final RevokeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        if (doesNotExist(request.account())) {
            logger.log("request did not contain an account");
            throw new CfnInvalidRequestException(request.toString());
        }

        // Update request will include a list of VPC ids. After the request is handled, that should be the list of
        // vpc ids remaining on the auth.
        try {
            List<String> existingVpcIds = getExistingVpcIds(
                    request.account(),
                    request.clusterIdentifier(),
                    proxyClient
            );
            List<String> vpcIdsInUpdateRequest = request.vpcIds();

            // This means that we are trying to authorize specific after doing an authorize all - not allowed
            if (existingVpcIds.isEmpty()) {
                // If we had authorized all before, we cannot go from auth all -> auth specific yet
                if (!vpcIdsInUpdateRequest.isEmpty()) {
                    throw new CfnInvalidRequestException(request.toString() + existingVpcIds);
                }
                // Otherwise, we are trying to do an idempotent update?
                return null;
            }

            List<String> vpcIdsToRemove = getVpcIdsToRemove(existingVpcIds, vpcIdsInUpdateRequest);
            // If there were no vpcs to remove
            if (vpcIdsToRemove.isEmpty()) {
                return null;
            }

            RevokeEndpointAccessRequest revokeRequest = RevokeEndpointAccessRequest.builder()
                    .clusterIdentifier(request.clusterIdentifier())
                    .account(request.account())
                    .vpcIds(vpcIdsToRemove)
                    .build();

            return proxyClient.injectCredentialsAndInvokeV2(
                    revokeRequest,
                    proxyClient.client()::revokeEndpointAccess
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
    }

    List<String> getExistingVpcIds(final String accountId,
                                   final String clusterId,
                                   final ProxyClient<RedshiftClient> proxyClient) {
        // Make a call to check the existing VPC ids that exist for the auth. Remove the ones that already
        // exist before making the API call, otherwise we get an error saying vpc id already exists.
        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(accountId)
                .clusterIdentifier(clusterId)
                .build();

        DescribeEndpointAuthorizationResponse describeResponse = null;


        describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                describeRequest, proxyClient.client()::describeEndpointAuthorization
        );

        logger.log("describe response when getting existing vpc ids: " + describeResponse);

        List<EndpointAuthorization> endpointAuthorizationList = describeResponse.endpointAuthorizationList();
        if (endpointAuthorizationList.isEmpty()) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    String.format("account:%s-clusteridentifier:%s",
                            accountId,
                            clusterId)
            );
        }

        return endpointAuthorizationList.get(0).allowedVPCs();
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
