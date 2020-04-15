package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetStateException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final RedshiftClient client = ClientBuilder.getClient();
        DeleteClusterSubnetGroupRequest deleteRequest = Translator.deleteClusterSubnetGroupRequest(model);

        try {
            proxy.injectCredentialsAndInvokeV2(deleteRequest, client::deleteClusterSubnetGroup);
            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, model.getSubnetGroupName()));
        } catch (ClusterSubnetGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getSubnetGroupName());
        } catch (InvalidClusterSubnetGroupStateException | InvalidClusterSubnetStateException e) {
            throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
