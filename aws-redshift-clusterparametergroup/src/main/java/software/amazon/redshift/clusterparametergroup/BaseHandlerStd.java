package software.amazon.redshift.clusterparametergroup;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected Logger logger;
    protected static final String CALL_GRAPH_TYPE_NAME = StringUtils.replace(ResourceModel.TYPE_NAME, "::", "-");

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger);

    protected DescribeTagsResponse readTags(final DescribeTagsRequest awsRequest,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        DescribeTagsResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeTags);
        logger.log(awsResponse.toString());
        logger.log(String.format("%s's tags have successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    protected CreateTagsResponse updateTags(final ModifyTagsRequest awsRequest,
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

    protected ProgressEvent<ResourceModel, CallbackContext> operateTagsErrorHandler(final Object awsRequest,
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
            return ProgressEvent.failed(model, context, HandlerErrorCode.UnauthorizedTaggingOperation, exception.getMessage());
        }
    }

}
