package software.amazon.redshift.scheduledaction;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.CreateScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.InvalidScheduleException;
import software.amazon.awssdk.services.redshift.model.InvalidScheduledActionException;
import software.amazon.awssdk.services.redshift.model.ScheduledActionAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ScheduledActionQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ScheduledActionTypeUnsupportedException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-ScheduledAction::Create", proxyClient, progress.getResourceModel(), callbackContext)
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createScheduledAction)
                                .handleError(this::createScheduledActionsErrorHandler)
                                .progress()
                )

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateScheduledActionResponse createScheduledAction(final CreateScheduledActionRequest awsRequest,
                                                                final ProxyClient<RedshiftClient> proxyClient) {
        CreateScheduledActionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createScheduledAction);

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createScheduledActionsErrorHandler(final CreateScheduledActionRequest awsRequest,
                                                                                             final Exception exception,
                                                                                             final ProxyClient<RedshiftClient> client,
                                                                                             final ResourceModel model,
                                                                                             final CallbackContext context) {
        if (exception instanceof ScheduledActionAlreadyExistsException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);

        } else if (exception instanceof ScheduledActionQuotaExceededException ||
                exception instanceof ScheduledActionTypeUnsupportedException ||
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
