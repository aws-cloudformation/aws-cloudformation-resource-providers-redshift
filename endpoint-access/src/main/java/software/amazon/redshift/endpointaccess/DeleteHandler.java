package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessResponse;
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
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Delete",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToDeleteRequest)
                        .makeServiceCall(this::deleteEndpointAccess)
                        .stabilize((_request, _response, client, model, ctx) ->
                                EndpointAccessStabilizers.isEndpointDeleted(client, model, ctx))
                        .done(response -> ProgressEvent.defaultSuccessHandler(null))
            );
    }

    private DeleteEndpointAccessResponse deleteEndpointAccess(
            final DeleteEndpointAccessRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteEndpointAccessResponse response;

        // TODO catch errors
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    deleteRequest, proxyClient.client()::deleteEndpointAccess
            );
        } catch (Exception e) {
            throw new CfnGeneralServiceException(deleteRequest.toString(), e);
        }

        return response;
    }
}
