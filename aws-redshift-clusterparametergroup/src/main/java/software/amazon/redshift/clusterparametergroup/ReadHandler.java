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
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Read::ReadInstance", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall(this::describeClusterParameterGroups)
                        .handleError(this::describeClusterParameterGroupsErrorHandler)
                        .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(Translator.translateFromReadResponse(awsResponse))
                                .status(OperationStatus.IN_PROGRESS)
                                .build())
                )

                .then(progress -> proxy.initiate(String.format("%s::Read::ReadParameters", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadParametersRequest)
                        .makeServiceCall(this::describeClusterParameters)
                        .handleError(this::describeClusterParametersErrorHandler)
                        .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadParametersResponse(awsResponse, progress.getResourceModel()))));
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
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameters);

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
