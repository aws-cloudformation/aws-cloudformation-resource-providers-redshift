package software.amazon.redshift.eventsubscription;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DeleteEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.InvalidSubscriptionStateException;
import software.amazon.awssdk.services.redshift.model.SubscriptionNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EventSubscription::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteEventSubscription)
                                .handleError(this::deleteEventSubscriptionErrorHandler)
                                .progress()
                )

                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteEventSubscriptionResponse deleteEventSubscription(final DeleteEventSubscriptionRequest awsRequest,
                                                                    final ProxyClient<RedshiftClient> proxyClient) {
        DeleteEventSubscriptionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteEventSubscription);

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteEventSubscriptionErrorHandler(final DeleteEventSubscriptionRequest awsRequest,
                                                                                              final Exception exception,
                                                                                              final ProxyClient<RedshiftClient> client,
                                                                                              final ResourceModel model,
                                                                                              final CallbackContext context) {
        if (exception instanceof SubscriptionNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else if (exception instanceof InvalidSubscriptionStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
