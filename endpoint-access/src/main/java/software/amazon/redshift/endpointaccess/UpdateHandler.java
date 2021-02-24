package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.redshift.endpointaccess.EndpointAccessStabilizers.isEndpointActive;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Update",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::modifyEndpointAccess)
                        .stabilize((awsRequest, response, client, model, ctx) -> isEndpointActive(client, model, ctx))
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyEndpointAccessResponse modifyEndpointAccess(
            final ModifyEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyEndpointAccessResponse response;

        // TODO catch more errors
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::modifyEndpointAccess
            );
        } catch (Exception e) {
            throw new CfnGeneralServiceException(request.toString(), e);
        }

        return response;
    }
}
