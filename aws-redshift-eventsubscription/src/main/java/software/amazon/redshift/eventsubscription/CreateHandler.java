package software.amazon.redshift.eventsubscription;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.CreateEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.EventSubscriptionQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.SnsInvalidTopicException;
import software.amazon.awssdk.services.redshift.model.SnsNoAuthorizationException;
import software.amazon.awssdk.services.redshift.model.SnsTopicArnNotFoundException;
import software.amazon.awssdk.services.redshift.model.SourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionAlreadyExistException;
import software.amazon.awssdk.services.redshift.model.SubscriptionCategoryNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionEventIdNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionSeverityNotFoundException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
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
                        proxy.initiate("AWS-Redshift-EventSubscription::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createEventSubscription)
                                .handleError(this::createEventSubscriptionErrorHandler)
                                .progress()
                )

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateEventSubscriptionResponse createEventSubscription(final CreateEventSubscriptionRequest awsRequest,
                                                                    final ProxyClient<RedshiftClient> proxyClient) {
        CreateEventSubscriptionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createEventSubscription);

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createEventSubscriptionErrorHandler(final CreateEventSubscriptionRequest awsRequest,
                                                                                              final Exception exception,
                                                                                              final ProxyClient<RedshiftClient> client,
                                                                                              final ResourceModel model,
                                                                                              final CallbackContext context) {
        if (exception instanceof SubscriptionAlreadyExistException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);

        } else if (exception instanceof SnsTopicArnNotFoundException ||
                exception instanceof SubscriptionEventIdNotFoundException ||
                exception instanceof SubscriptionCategoryNotFoundException ||
                exception instanceof SubscriptionSeverityNotFoundException ||
                exception instanceof SourceNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof EventSubscriptionQuotaExceededException ||
                exception instanceof SnsInvalidTopicException ||
                exception instanceof TagLimitExceededException ||
                exception instanceof InvalidTagException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else if (exception instanceof SnsNoAuthorizationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidCredentials);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
