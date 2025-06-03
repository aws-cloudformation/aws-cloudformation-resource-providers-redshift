package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
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

        // Validate primary identifier
        if (model == null || model.getClusterSubnetGroupName() == null) {
            return ProgressEvent.failed(null, callbackContext, HandlerErrorCode.NotFound,
                    "ClusterSubnetGroupName is required");
        }

        final String resourceName = String.format("arn:%s:redshift:%s:%s:subnetgroup:%s",
                request.getAwsPartition(),
                request.getRegion(),
                request.getAwsAccountId(),
                model.getClusterSubnetGroupName());

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Read::SubnetGroup", CALL_GRAPH_TYPE_NAME), proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall(this::readResource)
                        .handleError((awsRequest, exception, client, resourceModel, cxt) -> {
                            if (exception instanceof ClusterSubnetGroupNotFoundException) {
                                return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
                            }
                            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
                        })
                        .done(awsResponse -> {
                            ResourceModel updatedModel = Translator.translateFromReadResponse(awsResponse);
                            // Ensure primaryIdentifier is set
                            if (updatedModel.getClusterSubnetGroupName() == null) {
                                updatedModel.setClusterSubnetGroupName(
                                        model.getClusterSubnetGroupName());
                            }
                            return ProgressEvent.progress(updatedModel, callbackContext);
                        }))
                .then(progress -> proxy.initiate(String.format("%s::Read::Tags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), callbackContext)
                        .translateToServiceRequest(rm -> Translator.translateToReadTagsRequest(resourceName))
                        .makeServiceCall(this::readTags)  // Using inherited readTags method
                        .handleError(this::operateTagsErrorHandler)  // Using inherited error handler
                        .done((tagsRequest, tagsResponse, client, resourceModel, context) ->
                                ProgressEvent.defaultSuccessHandler(Translator.translateFromReadTagsResponse(resourceModel, tagsResponse))));
    }

    private DescribeClusterSubnetGroupsResponse readResource(
            final DescribeClusterSubnetGroupsRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {

        DescribeClusterSubnetGroupsResponse awsResponse = proxyClient.injectCredentialsAndInvokeV2(
                awsRequest,
                proxyClient.client()::describeClusterSubnetGroups);
        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}