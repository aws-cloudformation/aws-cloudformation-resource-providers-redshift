package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.redshift.clustersubnetgroup.Translator.buildReadResponseModel;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final RedshiftClient client = ClientBuilder.getClient();

        final DescribeClusterSubnetGroupsResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.readClusterSubnetGroupsRequest(model), client::describeClusterSubnetGroups);
        } catch (ClusterSubnetGroupNotFoundException | InvalidTagException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getSubnetGroupName());
        }

        // TODO : put your code here

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(buildReadResponseModel(response))
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
