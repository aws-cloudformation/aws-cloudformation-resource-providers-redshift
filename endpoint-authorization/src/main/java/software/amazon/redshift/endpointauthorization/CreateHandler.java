package software.amazon.redshift.endpointauthorization;


import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        // AWS-Redshift-EndpointAuthorization::Create
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createEndpointAuthorization)
                                .stabilize((_request, _response, client, model, ctx) ->
                                        Stabilizers.isAuthorized(client, model, ctx))
                                .progress())
                .then(progress ->
                        getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }

    @VisibleForTesting
    ReadHandler getReadHandler() {
        return new ReadHandler();
    }

    @VisibleForTesting
    AuthorizeEndpointAccessResponse createEndpointAuthorization(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        AuthorizeEndpointAccessResponse response = null;

        // TODO catch and throw more errors
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::authorizeEndpointAccess
            );
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return response;
    }
}
