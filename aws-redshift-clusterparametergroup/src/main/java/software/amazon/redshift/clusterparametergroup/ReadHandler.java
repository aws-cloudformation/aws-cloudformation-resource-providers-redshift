package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String resourceName = String.format("arn:%s:redshift:%s:%s:parametergroup:%s", request.getAwsPartition(), request.getRegion(), request.getAwsAccountId(), model.getParameterGroupName());

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Read::ReadParameterGroup", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall(this::describeClusterParameterGroups)
                        .handleError(this::describeClusterParameterGroupsErrorHandler)
                        .done(awsResponse -> {
                            return ProgressEvent.progress(Translator.translateFromReadResponse(awsResponse), callbackContext);
                        })
                )
                .then(progress -> proxy.initiate(String.format("%s::Read::ReadParameters", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadParametersRequest)
                        .makeServiceCall(this::describeClusterParameters)
                        .handleError(this::describeClusterParametersErrorHandler)
                        .done(awsResponse -> {
                            return ProgressEvent.progress(Translator.translateFromReadParametersResponse(awsResponse, progress.getResourceModel()), callbackContext);
                        })
                )
                .then(progress -> proxy.initiate(String.format("%s::Read::ReadTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(resourceModel -> Translator.translateToReadTagsRequest(resourceName))
                        .makeServiceCall(this::readTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, resourceModel, context) -> {
                            return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadTagsResponse(resourceModel, tagsResponse));
                        }));
    }

    private DescribeClusterParameterGroupsResponse describeClusterParameterGroups(final DescribeClusterParameterGroupsRequest awsRequest,
                                                                                  final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClusterParameterGroupsResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameterGroups);

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeClusterParameterGroupsErrorHandler(final DescribeClusterParameterGroupsRequest awsRequest,
                                                                                                     final Exception exception,
                                                                                                     final ProxyClient<RedshiftClient> client,
                                                                                                     final ResourceModel model,
                                                                                                     final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidTagException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private DescribeClusterParametersResponse describeClusterParameters(final DescribeClusterParametersRequest awsRequest,
                                                                        final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClusterParametersResponse awsResponse;
        logger.log("Describe Cluster Parameters before call");
        logger.log(awsRequest.toString());
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameters);

        logger.log(awsResponse.toString());
        logger.log(String.format("%s's Parameters has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeClusterParametersErrorHandler(final DescribeClusterParametersRequest awsRequest,
                                                                                                final Exception exception,
                                                                                                final ProxyClient<RedshiftClient> client,
                                                                                                final ResourceModel model,
                                                                                                final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
