package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetStateException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                    proxy.initiate("AWS-Redshift-ClusterSubnetGroup::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteResource)
                    .success());
    }

    private DeleteClusterSubnetGroupResponse deleteResource(
        final DeleteClusterSubnetGroupRequest deleteRequest,
        final ProxyClient<RedshiftClient> proxyClient) {
        DeleteClusterSubnetGroupResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteClusterSubnetGroup);
            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, deleteRequest.clusterSubnetGroupName()));
        } catch (final ClusterSubnetGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.clusterSubnetGroupName());
        } catch (final InvalidClusterSubnetGroupStateException | InvalidClusterSubnetStateException e) {
            throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
