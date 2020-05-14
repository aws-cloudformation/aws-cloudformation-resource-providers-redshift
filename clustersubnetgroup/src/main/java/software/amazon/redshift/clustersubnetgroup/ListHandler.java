package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();
        DescribeClusterSubnetGroupsResponse describeClusterSubnetGroupsResponse = null;
        try {
            describeClusterSubnetGroupsResponse =
                    proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
                            ClientBuilder.getClient()::describeClusterSubnetGroups);

        } catch (final ClusterSubnetGroupNotFoundException | InvalidTagException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getSubnetGroupName());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListResponse(describeClusterSubnetGroupsResponse))
            .nextToken(describeClusterSubnetGroupsResponse.marker())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
