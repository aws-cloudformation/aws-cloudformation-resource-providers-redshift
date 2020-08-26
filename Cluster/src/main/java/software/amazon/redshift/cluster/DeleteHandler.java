package software.amazon.redshift.cluster;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
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
                        proxy.initiate("AWS-Redshift-Cluster::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .success());

    }

    private DeleteClusterResponse deleteResource(
            final DeleteClusterRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteClusterResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteCluster);
            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, deleteRequest.clusterIdentifier()));
        } catch (final ClusterNotFoundException | ClusterSnapshotAlreadyExistsException | ClusterSnapshotQuotaExceededException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException | InvalidRetentionPeriodException e) {
            throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(deleteRequest.toString(), e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
