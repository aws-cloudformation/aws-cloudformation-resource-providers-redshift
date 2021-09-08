package software.amazon.redshift.endpointauthorization;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidAuthorizationStateException;
import software.amazon.awssdk.services.redshift.model.InvalidEndpointStateException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(
                            "AWS-Redshift-EndpointAuthorization::Delete",
                            proxyClient,
                            progress.getResourceModel(),
                            progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToRevokeRequest)
                        .makeServiceCall(this::revokeEndpointAccess)
                        .done(response -> ProgressEvent.defaultSuccessHandler(null))
                );
    }

    @VisibleForTesting
    RevokeEndpointAccessResponse revokeEndpointAccess(
            final RevokeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        RevokeEndpointAccessResponse response;

        // Validate that the account is not null
        if (!Validator.doesExist(request.account())) {
            throw new CfnInvalidRequestException(request.toString());
        }

        logAPICall(request, "RevokeEndpointAccess", logger);

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::revokeEndpointAccess
            );
        } catch (EndpointAuthorizationNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (ClusterNotFoundException
                | InvalidAuthorizationStateException
                | InvalidEndpointStateException
                | InvalidParameterValueException e) {
            throw new CfnInvalidRequestException(e);
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(e);
        }

        return response;
    }
}
