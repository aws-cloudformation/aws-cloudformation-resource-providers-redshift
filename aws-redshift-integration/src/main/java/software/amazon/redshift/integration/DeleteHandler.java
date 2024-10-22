package software.amazon.redshift.integration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DeleteIntegrationRequest;
import software.amazon.awssdk.services.redshift.model.IntegrationNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;

public class DeleteHandler extends BaseHandlerStd {
    // Currently, if you re-create an Integration within 500 seconds of deletion against the same cluster,
    // The Integration may fail to create. Remove when the issue no longer exists.
    // todo: verify if we have the same issue
    static final int POST_DELETION_DELAY_SEC = 30;
    static final int PRE_DELETION_DELAY_SEC = 30;
    static final int CALLBACK_DELAY = 6;

    /** Default constructor w/ default backoff */
    public DeleteHandler() {}

    /** Default constructor w/ custom config */
    public DeleteHandler(HandlerConfig config) {
        super(config);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {
        return checkIfIntegrationExists(proxy, request, callbackContext, proxyClient)
                .then((evt) -> proxy.initiate("rds::delete-integration", proxyClient, request.getDesiredResourceState(), callbackContext)
                        .translateToServiceRequest(DeleteHandler::deleteIntegrationRequest)
                        .backoffDelay(config.getBackoff())
                        .makeServiceCall((deleteIntegrationRequest, proxyInvocation) ->
                                proxyInvocation.injectCredentialsAndInvokeV2(deleteIntegrationRequest, proxyInvocation.client()::deleteIntegration))
                        .stabilize((deleteIntegrationRequest, deleteIntegrationResponse, proxyInvocation, model, context) -> isDeleted(model, proxyInvocation))
                        .handleError((deleteRequest, exception, client, resourceModel, ctx) -> {
                            // if the integration is already deleted, this should be ignored,
                            // but only once we started the deletion process
                            if (exception instanceof IntegrationNotFoundException) {
                                return ProgressEvent.defaultSuccessHandler(resourceModel);
                            }

                            return handleIntegrationException(exception);
                        })
                        .progress()
                        .then((e) -> delay(e, POST_DELETION_DELAY_SEC))
                        .then((e) -> ProgressEvent.defaultSuccessHandler(null)));
    }

    private static DeleteIntegrationRequest deleteIntegrationRequest(final ResourceModel model) {
        return DeleteIntegrationRequest.builder()
                .integrationArn(model.getIntegrationArn())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkIfIntegrationExists(final AmazonWebServicesClientProxy proxy,
                                                                                   final ResourceHandlerRequest<ResourceModel> request,
                                                                                   final CallbackContext callbackContext,
                                                                                   final ProxyClient<RedshiftClient> proxyClient) {
        // it is part of the CFN contract that we return NotFound on DELETE.
        return proxy.initiate("redshift::delete-integration-check-exists", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(ReadHandler::describeIntegrationsRequest)
                .backoffDelay(config.getBackoff())
                .makeServiceCall(((describeIntegrationsRequest, proxyInvocation) ->
                        proxyInvocation.injectCredentialsAndInvokeV2(describeIntegrationsRequest, proxyInvocation.client()::describeIntegrations)))
                .handleError((deleteRequest, exception, client, resourceModel, ctx) -> handleIntegrationException(exception))
                .progress();
    }

    /** Inserts an artificial delay */
    private ProgressEvent<ResourceModel, CallbackContext> delay(final ProgressEvent<ResourceModel, CallbackContext> evt, final int seconds) {
        CallbackContext callbackContext = evt.getCallbackContext();
        if (callbackContext.getDeleteWaitTime() <= seconds) {
            callbackContext.setDeleteWaitTime(callbackContext.getDeleteWaitTime() + CALLBACK_DELAY);
            return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY, evt.getResourceModel());
        } else {
            return ProgressEvent.progress(evt.getResourceModel(), callbackContext);
        }
    }

    protected boolean isDeleted(final ResourceModel model,
                                final ProxyClient<RedshiftClient> proxyClient) {
        try {
            proxyClient.injectCredentialsAndInvokeV2(ReadHandler.describeIntegrationsRequest(model), proxyClient.client()::describeIntegrations);
            return false;
        } catch (IntegrationNotFoundException e) {
            return true;
        }
    }

}
