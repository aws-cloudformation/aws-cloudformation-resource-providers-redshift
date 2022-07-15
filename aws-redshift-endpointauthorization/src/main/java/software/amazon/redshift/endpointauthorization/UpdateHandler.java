package software.amazon.redshift.endpointauthorization;

import com.amazonaws.SdkClientException;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationsPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidAuthorizationStateException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest((model) ->
                                        Translator.translateToUpdateAuthorizeRequest(model, proxyClient))
                                .makeServiceCall(this::authorizeEndpointAccess)
                                .progress())
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Delete",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest((model) ->
                                        Translator.translateToUpdateRevokeRequest(model, proxyClient))
                                .makeServiceCall(this::revokeEndpointAuthorization)
                                .progress())
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }

    @VisibleForTesting
    AuthorizeEndpointAccessResponse authorizeEndpointAccess(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {

        if (request == null) {
            return null;
        }

        if (!Validator.doesExist(request.account())) {
            logger.log("request did not contain an account");
            throw new CfnInvalidRequestException(request.toString());
        }

        // Update request will include a list of VPC ids. After the request is handled, that should be the list of
        // vpc ids remaining on the auth.
        try {
            logAPICall(request, "AuthorizeEndpointAccess", logger);

            return proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::authorizeEndpointAccess
            );
        } catch (EndpointAuthorizationAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ClusterNotFoundException | InvalidParameterValueException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(e);
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

        if (request == null) {
            return null;
        }

        if (!Validator.doesExist(request.account())) {
            logger.log("request did not contain an account");
            throw new CfnInvalidRequestException(request.toString());
        }

        // Update request will include a list of VPC ids. After the request is handled, that should be the list of
        // vpc ids remaining on the auth.
        try {
            logAPICall(request, "RevokeEndpointAccess", logger);

            return proxyClient.injectCredentialsAndInvokeV2(
                    request,
                    proxyClient.client()::revokeEndpointAccess
            );
        } catch (EndpointAuthorizationAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ClusterNotFoundException | InvalidParameterValueException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (InvalidAuthorizationStateException e) {
            throw new CfnResourceConflictException(e);
        } catch (EndpointAuthorizationsPerClusterLimitExceededException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (RedshiftException | SdkClientException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
    }
}
