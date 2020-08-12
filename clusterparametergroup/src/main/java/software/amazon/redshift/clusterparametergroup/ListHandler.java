package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.*;
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

        final DescribeClusterParameterGroupsRequest awsRequest = Translator.translateToListRequest(request.getNextToken());

        DescribeClusterParameterGroupsResponse awsResponse = null;
        try {
            proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeClusterParameterGroups);

        } catch (ClusterParameterGroupNotFoundException | InvalidTagException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getParameterGroupName());
        };

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(Translator.translateFromListResponse(awsResponse))
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
