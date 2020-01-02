package software.amazon.redshift.clusterparametergroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroup;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.Parameter;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        // TODO : put your code here

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    static String getArn(final String parameterGroupName, final ResourceHandlerRequest<ResourceModel> request) {
        // TODO: use request.getAwsPartition() once implemented
        String partition = "aws";
        if (request.getRegion().contains("us-gov-")) partition.concat("-us-gov");
        if (request.getRegion().contains("cn-")) partition.concat("-cn");
        return String.format("arn:%s:redshift:%s:%s:parametergroup:%s", partition, request.getRegion(), request.getAwsAccountId(), parameterGroupName);
    }

    static List<Tag> getTags(final String arn, final AmazonWebServicesClientProxy proxy, final RedshiftClient client) {
        final DescribeTagsResponse response = proxy.injectCredentialsAndInvokeV2(Translator.describeTagsRequest(arn), client::describeTags);
        return response.taggedResources().stream().map(TaggedResource::tag).collect(Collectors.toList());
    }
}