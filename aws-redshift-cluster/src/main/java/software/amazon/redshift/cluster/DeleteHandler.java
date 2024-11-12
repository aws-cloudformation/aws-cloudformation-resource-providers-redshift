package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
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
        final ProxyClient<SecretsManagerClient> secretsManagerProxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
            .then(progress -> {
                if (!callbackContext.getCallBackForDelete()) {
                    callbackContext.setCallBackForDelete(true);
                    logger.log ("In Delete, Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds");
                    progress = ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, model);
                }
                return progress;
            })
            .then(progress -> {
                // Set the secret ARN in the callback context. We will use this in the stabilize operation of this handler
                if (callbackContext.getMasterPasswordSecretArn() == null) {
                    String masterPasswordSecretArn = getClusterSecretArn(proxyClient, model.getClusterIdentifier());
                    callbackContext.setMasterPasswordSecretArn(masterPasswordSecretArn);
                }
                progress = proxy.initiate("AWS-Redshift-Cluster::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest((_model) -> Translator.translateToDeleteRequest(_model, request.getSnapshotRequested()))
                    .makeServiceCall(this::deleteResource)
                    .stabilize((_request, _response, _client, _model, _context) -> isClusterActiveAfterDelete(_client, _model, _context) &&
                            isClusterSecretDeleted(secretsManagerProxyClient, _context))
                    .done((response) -> {
                        logger.log(String.format("%s %s deleted.", ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
                        return ProgressEvent.defaultSuccessHandler(null);
                    });

                return progress;
            });

    }

    private DeleteClusterResponse deleteResource(
            final DeleteClusterRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteClusterResponse awsResponse = null;
        try {
            logger.log(String.format("%s %s deleteCluster", ResourceModel.TYPE_NAME, deleteRequest.clusterIdentifier()));
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteCluster);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.clusterIdentifier(), e);
        } catch (final InvalidClusterStateException | InvalidRetentionPeriodException | ClusterSnapshotAlreadyExistsException | ClusterSnapshotQuotaExceededException e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s Deleting", ResourceModel.TYPE_NAME,
                deleteRequest.clusterIdentifier()));

        return awsResponse;
    }
}
