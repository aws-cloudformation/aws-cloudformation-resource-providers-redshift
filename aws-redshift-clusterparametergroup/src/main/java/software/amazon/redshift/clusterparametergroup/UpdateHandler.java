package software.amazon.redshift.clusterparametergroup;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
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
        final String resourceName = String.format("arn:%s:redshift:%s:%s:parametergroup:%s", request.getAwsPartition(), request.getRegion(), request.getAwsAccountId(), request.getDesiredResourceState().getParameterGroupName());

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

                .then(progress -> proxy.initiate("AWS-Redshift-ClusterParameterGroup::Update::ResetInstance", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToResetRequest)
                        .makeServiceCall(this::resetClusterParameterGroup)
                        .handleError(this::resetClusterParameterGroupErrorHandler)
                        .progress()
                )

                .then(progress -> CollectionUtils.isEmpty(progress.getResourceModel().getParameters()) ? progress :
                        proxy.initiate("AWS-Redshift-ClusterParameterGroup::Update::UpdateInstance", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall(this::modifyClusterParameterGroup)
                                .handleError(this::modifyClusterParameterGroupErrorHandler)
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

    private ResetClusterParameterGroupResponse resetClusterParameterGroup(final ResetClusterParameterGroupRequest awsRequest,
                                                                          final ProxyClient<RedshiftClient> proxyClient) {
        ResetClusterParameterGroupResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::resetClusterParameterGroup);

        logger.log(String.format("%s has successfully been reset.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> resetClusterParameterGroupErrorHandler(final ResetClusterParameterGroupRequest awsRequest,
                                                                                                 final Exception exception,
                                                                                                 final ProxyClient<RedshiftClient> client,
                                                                                                 final ResourceModel model,
                                                                                                 final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidClusterParameterGroupStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private ModifyClusterParameterGroupResponse modifyClusterParameterGroup(final ModifyClusterParameterGroupRequest awsRequest,
                                                                            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterParameterGroupResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::modifyClusterParameterGroup);

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> modifyClusterParameterGroupErrorHandler(final ModifyClusterParameterGroupRequest awsRequest,
                                                                                                  final Exception exception,
                                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                                  final ResourceModel model,
                                                                                                  final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidClusterParameterGroupStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
