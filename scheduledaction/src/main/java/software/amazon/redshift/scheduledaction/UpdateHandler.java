package software.amazon.redshift.scheduledaction;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.InvalidScheduleException;
import software.amazon.awssdk.services.redshift.model.InvalidScheduledActionException;
import software.amazon.awssdk.services.redshift.model.ModifyScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.ScheduledActionNotFoundException;
import software.amazon.awssdk.services.redshift.model.ScheduledActionTypeUnsupportedException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
                .then(progress ->
                        proxy.initiate("AWS-Redshift-ScheduledAction::Update", proxyClient, progress.getResourceModel(), callbackContext)
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall(this::modifyScheduledAction)
                                .handleError(this::modifyScheduledActionsErrorHandler)
                                .progress()
                )

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyScheduledActionResponse modifyScheduledAction(final ModifyScheduledActionRequest awsRequest,
                                                                final ProxyClient<RedshiftClient> proxyClient) {
        ModifyScheduledActionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::modifyScheduledAction);

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> modifyScheduledActionsErrorHandler(final ModifyScheduledActionRequest awsRequest,
                                                                                             final Exception exception,
                                                                                             final ProxyClient<RedshiftClient> client,
                                                                                             final ResourceModel model,
                                                                                             final CallbackContext context) {
        if (exception instanceof ScheduledActionNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof ScheduledActionTypeUnsupportedException ||
                exception instanceof InvalidScheduleException ||
                exception instanceof InvalidScheduledActionException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else if (exception instanceof UnauthorizedOperationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidCredentials);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
