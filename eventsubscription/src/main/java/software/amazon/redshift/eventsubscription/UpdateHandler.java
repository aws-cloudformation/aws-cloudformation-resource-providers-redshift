package software.amazon.redshift.eventsubscription;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidSubscriptionStateException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.SnsInvalidTopicException;
import software.amazon.awssdk.services.redshift.model.SnsNoAuthorizationException;
import software.amazon.awssdk.services.redshift.model.SnsTopicArnNotFoundException;
import software.amazon.awssdk.services.redshift.model.SourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionCategoryNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionEventIdNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubscriptionSeverityNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
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
        final String resourceName = "arn:aws:redshift:" + request.getRegion() + ":" + request.getAwsAccountId() + ":eventsubscription:" + request.getDesiredResourceState().getSubscriptionName();

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EventSubscription::Update::ReadTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(resourceModel -> Translator.translateToReadTagsRequest(resourceName))
                                .makeServiceCall(this::readTags)
                                .handleError(this::operateTagsErrorHandler)
                                .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .callbackContext(callbackContext)
                                        .callbackDelaySeconds(0)
                                        .resourceModel(Translator.translateFromReadTagsResponse(tagsResponse))
                                        .status(OperationStatus.IN_PROGRESS)
                                        .build()))

                .then(progress ->
                        proxy.initiate("AWS-Redshift-EventSubscription::Update::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(resourceModel -> Translator.translateToUpdateTagsRequest(request.getDesiredResourceState(), resourceModel, resourceName))
                                .makeServiceCall(this::updateTags)
                                .handleError(this::operateTagsErrorHandler)
                                .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .callbackContext(callbackContext)
                                        .callbackDelaySeconds(0)
                                        .resourceModel(request.getDesiredResourceState())
                                        .status(OperationStatus.IN_PROGRESS)
                                        .build()))

                .then(progress ->
                        proxy.initiate("AWS-Redshift-EventSubscription::Update::UpdateInstance", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall(this::modifyEventSubscription)
                                .handleError(this::modifyEventSubscriptionErrorHandler)
                                .progress())

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private DescribeTagsResponse readTags(final DescribeTagsRequest awsRequest,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        DescribeTagsResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeTags);

        logger.log(String.format("%s's tags have successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private CreateTagsResponse updateTags(final ModifyTagsRequest awsRequest,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        CreateTagsResponse awsResponse = null;

        if (awsRequest.getDeleteOldTagsRequest().tagKeys().isEmpty()) {
            logger.log(String.format("No tags would be deleted for the resource: %s.", ResourceModel.TYPE_NAME));

        } else {
            proxyClient.injectCredentialsAndInvokeV2(awsRequest.getDeleteOldTagsRequest(), proxyClient.client()::deleteTags);
            logger.log(String.format("Delete tags for the resource: %s.", ResourceModel.TYPE_NAME));
        }

        if (awsRequest.getCreateNewTagsRequest().tags().isEmpty()) {
            logger.log(String.format("No tags would be created for the resource: %s.", ResourceModel.TYPE_NAME));

        } else {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest.getCreateNewTagsRequest(), proxyClient.client()::createTags);
            logger.log(String.format("Create tags for the resource: %s.", ResourceModel.TYPE_NAME));
        }

        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> operateTagsErrorHandler(final Object awsRequest,
                                                                                  final Exception exception,
                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                  final ResourceModel model,
                                                                                  final CallbackContext context) {
        if (exception instanceof ResourceNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidTagException ||
                exception instanceof InvalidClusterStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private ModifyEventSubscriptionResponse modifyEventSubscription(final ModifyEventSubscriptionRequest awsRequest,
                                                                    final ProxyClient<RedshiftClient> proxyClient) {
        ModifyEventSubscriptionResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::modifyEventSubscription);

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }


    private ProgressEvent<ResourceModel, CallbackContext> modifyEventSubscriptionErrorHandler(final ModifyEventSubscriptionRequest awsRequest,
                                                                                              final Exception exception,
                                                                                              final ProxyClient<RedshiftClient> client,
                                                                                              final ResourceModel model,
                                                                                              final CallbackContext context) {
        if (exception instanceof SubscriptionNotFoundException ||
                exception instanceof SnsTopicArnNotFoundException ||
                exception instanceof SubscriptionEventIdNotFoundException ||
                exception instanceof SubscriptionCategoryNotFoundException ||
                exception instanceof SubscriptionSeverityNotFoundException ||
                exception instanceof SourceNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof SnsInvalidTopicException ||
                exception instanceof InvalidSubscriptionStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else if (exception instanceof SnsNoAuthorizationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidCredentials);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
