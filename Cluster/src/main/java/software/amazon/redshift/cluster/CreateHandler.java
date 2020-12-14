package software.amazon.redshift.cluster;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.CreateUsageLimitRequest;
import software.amazon.awssdk.services.redshift.model.CreateUsageLimitResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotResponse;
import software.amazon.awssdk.services.redshift.model.RestoreTableFromClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.RestoreTableFromClusterSnapshotResponse;
import software.amazon.awssdk.services.redshift.model.TableRestoreNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.UUID;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_CLUSTER_IDENTIFIER_LENGTH = 63;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        prepareResourceModel(request);
        final ResourceModel resourceModel = request.getDesiredResourceState();

        if (resourceModel.getRedshiftCommand() != null && resourceModel.getRedshiftCommand().equals("restore-from-cluster-snapshot")) {
            return ProgressEvent.progress(resourceModel, callbackContext)
                    .then(progress -> proxy.initiate("AWS-Redshift-Cluster::RestoreFromClusterSnapshot", proxyClient, resourceModel, callbackContext)
                            .translateToServiceRequest((m) -> Translator.translateToRestoreFromClusterSnapshotRequest(resourceModel))
                            .makeServiceCall(this::restoreFromClusterSnapshot)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress())
                    .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        } else if (resourceModel.getRedshiftCommand() != null && resourceModel.getRedshiftCommand().equals("restore-table-from-cluster-snapshot")) {
            return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-Cluster::RestoreTableFromClusterSnapshot", proxyClient, resourceModel, callbackContext)
                        .translateToServiceRequest((m) -> Translator.translateToRestoreTableFromClusterSnapshotRequest(resourceModel))
                        .makeServiceCall(this::restoreTableFromClusterSnapshot)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress())
                        .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        } else if (resourceModel.getRedshiftCommand() != null && resourceModel.getRedshiftCommand().equals("create-usage-limit")) {
            boolean clusterExists = isClusterAvailableForNextOperation(proxyClient, resourceModel, resourceModel.getClusterIdentifier());
            if(!clusterExists) {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.FAILED)
                        .errorCode(HandlerErrorCode.NotFound)
                        .message(HandlerErrorCode.NotFound.getMessage())
                        .build();
            } else {
                return ProgressEvent.progress(resourceModel, callbackContext)
                        .then(progress -> proxy.initiate("AWS-Redshift-Cluster::CreateClusterUsageLimit", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest((m) -> Translator.translateToCreateUsageLimitRequest(resourceModel))
                                .makeServiceCall(this::clusterUsageLimit)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress())
                        .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
            }
        }

        if(invalidCreateClusterRequest(resourceModel)) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.InvalidRequest)
                    .message(HandlerErrorCode.InvalidRequest.getMessage())
                    .build();
        }

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-Cluster::Create", proxyClient, resourceModel, callbackContext)
                        .translateToServiceRequest((m) -> Translator.translateToCreateRequest(resourceModel))
                        .makeServiceCall(this::createClusterResource)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateClusterResponse createClusterResource(
            final CreateClusterRequest createRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterResponse createResponse = null;

        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(createRequest, proxyClient.client()::createCluster);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException e) {
            throw new CfnInvalidRequestException(createRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(createRequest.toString(), e);
        }
        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));

        return createResponse;
    }

    private RestoreFromClusterSnapshotResponse restoreFromClusterSnapshot(
            final RestoreFromClusterSnapshotRequest restoreFromClusterSnapshotRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RestoreFromClusterSnapshotResponse restoreFromClusterSnapshotResponse = null;

        try {
            restoreFromClusterSnapshotResponse = proxyClient.injectCredentialsAndInvokeV2(restoreFromClusterSnapshotRequest,
                    proxyClient.client()::restoreFromClusterSnapshot);
        } catch (final ClusterAlreadyExistsException | ClusterSnapshotAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, restoreFromClusterSnapshotRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException | ClusterSnapshotNotFoundException e) {
            throw new CfnInvalidRequestException(restoreFromClusterSnapshotRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(restoreFromClusterSnapshotRequest.toString(), e);
        }
        logger.log(String.format("%s Restore Cluster from Snapshot.", ResourceModel.TYPE_NAME));

        return restoreFromClusterSnapshotResponse;
    }

    private RestoreTableFromClusterSnapshotResponse restoreTableFromClusterSnapshot(
            final RestoreTableFromClusterSnapshotRequest restoreTableFromClusterSnapshotRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RestoreTableFromClusterSnapshotResponse restoreTableFromClusterSnapshotResponse = null;

        try {
            restoreTableFromClusterSnapshotResponse = proxyClient.injectCredentialsAndInvokeV2(restoreTableFromClusterSnapshotRequest,
                    proxyClient.client()::restoreTableFromClusterSnapshot);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, restoreTableFromClusterSnapshotRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException
                | ClusterSnapshotNotFoundException | TableRestoreNotFoundException e) {
            throw new CfnInvalidRequestException(restoreTableFromClusterSnapshotRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(restoreTableFromClusterSnapshotRequest.toString(), e);
        }
        logger.log(String.format("%s Restore Table from Cluster Snapshot.", ResourceModel.TYPE_NAME));

        return restoreTableFromClusterSnapshotResponse;
    }

    private CreateUsageLimitResponse clusterUsageLimit(
            final CreateUsageLimitRequest createUsageLimitRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateUsageLimitResponse createUsageLimitResponse = null;

        try {
            createUsageLimitResponse = proxyClient.injectCredentialsAndInvokeV2(createUsageLimitRequest,
                    proxyClient.client()::createUsageLimit);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createUsageLimitRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException
                | ClusterSnapshotNotFoundException | TableRestoreNotFoundException e) {
            throw new CfnInvalidRequestException(createUsageLimitRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(createUsageLimitRequest.toString(), e);
        }
        logger.log(String.format("%s Restore Table from Cluster Snapshot.", ResourceModel.TYPE_NAME));

        return createUsageLimitResponse;
    }

    private void prepareResourceModel(ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }
        final ResourceModel model = request.getDesiredResourceState();

        String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                ? "cluster-" + UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();

        if (StringUtils.isNullOrEmpty(model.getClusterIdentifier())) {
            model.setClusterIdentifier(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_CLUSTER_IDENTIFIER_LENGTH
                    ).toLowerCase()
            );
        }
    }
}
