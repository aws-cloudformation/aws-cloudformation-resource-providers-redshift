package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteUsageLimitRequest;
import software.amazon.awssdk.services.redshift.model.DeleteUsageLimitResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.UsageLimitNotFoundException;
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
                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("delete-usage-limit")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DeleteUsageLimit", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteUsageLimitRequest)
                                .makeServiceCall(this::deleteUsageLimit)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done((response) -> ProgressEvent.defaultSuccessHandler(null));
                    }
                    return progress;
                })
                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("delete-tags")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DeleteTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteTagsRequest)
                                .makeServiceCall(this::deleteTags)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done((response) -> ProgressEvent.defaultSuccessHandler(null));
                    }
                    return progress;
                })
                .then(progress -> {
                        return proxy.initiate("AWS-Redshift-Cluster::DeleteCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActiveAfterDelete(_client, _model, _context))
                                .done((response) -> ProgressEvent.defaultSuccessHandler(null));
                });

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

    private DeleteUsageLimitResponse deleteUsageLimit(
            final DeleteUsageLimitRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteUsageLimitResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteUsageLimit);
            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, deleteRequest.usageLimitId()));
        } catch (final ClusterNotFoundException | UsageLimitNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.usageLimitId());
        } catch (final InvalidClusterStateException | InvalidRetentionPeriodException e) {
            throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(deleteRequest.toString(), e);
        }

        logger.log(String.format("%s deleted usage limit.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private DeleteTagsResponse deleteTags(
            final DeleteTagsRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteTagsResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteTags);
            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, deleteRequest.resourceName()));
        } catch (final ResourceNotFoundException  e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.resourceName());
        } catch (final InvalidTagException e) {
            throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(deleteRequest.toString(), e);
        }

        logger.log(String.format("%s deleted tag.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }
}
