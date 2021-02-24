package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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
        // TODO - do we need to do a pre-existence check? Why does the cluster create API not do that? I guess we throw
        // something already exists type of error?
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Create",
                            proxyClient,
                            progress.getResourceModel(),
                            progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToCreateRequest)
                        .makeServiceCall(this::createEndpointAccess)
                        .stabilize((_request, _response, client, model, ctx) ->
                                EndpointAccessStabilizers.isEndpointActive(client, model, ctx))
                        .progress())
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }

    private CreateEndpointAccessResponse createEndpointAccess(
            final CreateEndpointAccessRequest createRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateEndpointAccessResponse createResponse = null;

        // TODO catch and throw more errors etc
        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createEndpointAccess
            );
        } catch (Exception e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        }

        return createResponse;
    }
}
