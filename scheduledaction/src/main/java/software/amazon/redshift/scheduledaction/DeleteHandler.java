package software.amazon.redshift.scheduledaction;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DeleteScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.DeleteScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.ScheduledActionNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
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
                        proxy.initiate("AWS-Redshift-ScheduledAction::Delete", proxyClient, progress.getResourceModel(), callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteScheduledAction)
                                .handleError(this::deleteScheduledActionErrorHandler)
                                .progress()
                )

                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteScheduledActionResponse deleteScheduledAction(final DeleteScheduledActionRequest awsRequest,
                                                                final ProxyClient<RedshiftClient> proxyClient) {
        DeleteScheduledActionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteScheduledAction);

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteScheduledActionErrorHandler(final DeleteScheduledActionRequest awsRequest,
                                                                                            final Exception exception,
                                                                                            final ProxyClient<RedshiftClient> client,
                                                                                            final ResourceModel model,
                                                                                            final CallbackContext context) {
        if (exception instanceof ScheduledActionNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else if (exception instanceof UnauthorizedOperationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidCredentials);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
