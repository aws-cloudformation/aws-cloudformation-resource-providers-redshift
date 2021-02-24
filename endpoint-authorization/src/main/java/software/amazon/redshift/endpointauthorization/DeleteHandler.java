package software.amazon.redshift.endpointauthorization;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
                        .stabilize((_req, _resp, client, model, ctx) -> Stabilizers.isDoneRevoking(client, model, ctx))
                        .done(response -> ProgressEvent.defaultSuccessHandler(null))
                );
    }

    @VisibleForTesting
    RevokeEndpointAccessResponse revokeEndpointAccess(
            final RevokeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        RevokeEndpointAccessResponse response;
        // TODO catch more errors

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::revokeEndpointAccess
            );
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(request.toString(), e);
        }

        return response;
    }
}
