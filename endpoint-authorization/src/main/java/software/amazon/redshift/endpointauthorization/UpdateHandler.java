package software.amazon.redshift.endpointauthorization;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

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

        if (Optional.ofNullable(resourceModel.getRevoke()).orElse(false)) {
            return getDeleteHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger);
        }

        return getCreateHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger);
    }

    @VisibleForTesting
    DeleteHandler getDeleteHandler() {
        return new DeleteHandler();
    }

    @VisibleForTesting
    CreateHandler getCreateHandler() {
        return new CreateHandler();
    }
}
