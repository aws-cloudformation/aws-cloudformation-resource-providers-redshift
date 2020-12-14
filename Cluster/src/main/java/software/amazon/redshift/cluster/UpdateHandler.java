package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
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

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        boolean clusterAvailableForNextOperation = isClusterAvailableForNextOperation(proxyClient, model, model.getClusterIdentifier());
        if(!clusterAvailableForNextOperation) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(HandlerErrorCode.NotFound.getMessage())
                    .build();
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress -> {
                if(!CollectionUtils.isNullOrEmpty(model.getAddIamRoles()) || !CollectionUtils.isNullOrEmpty(model.getRemoveIamRoles())) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateClusterIAMRoles", proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToUpdateIAMRolesRequest)
                        .makeServiceCall(this::updateIAMRoles)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(issueModifyClusterRequest(model) && model.getRedshiftCommand().equals("modify-cluster")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToUpdateRequest)
                            .makeServiceCall(this::updateCluster)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("reboot-cluster")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-RebootCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToRebootClusterRequest)
                            .makeServiceCall(this::rebootCluster)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("pause-cluster")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-PauseCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToPauseClusterRequest)
                            .makeServiceCall(this::pauseCluster)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterPaused(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("resume-cluster")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ResumeCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToResumeClusterRequest)
                            .makeServiceCall(this::resumeCluster)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("modify-cluster-db-revision")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ModifyClusterDbRevision", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToModifyClusterDbRevisionRequest)
                            .makeServiceCall(this::modifyClusterDbRevision)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("modify-cluster-maintenance")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ModifyClusterMaintenance", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToModifyClusterMaintenanceRequest)
                            .makeServiceCall(this::modifyClusterMaintenance)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("enable-snapshot-copy")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-EnableSnapshotCopy", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToEnableSnapshotRequest)
                            .makeServiceCall(this::enableSnapshotCopy)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("disable-snapshot-copy")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-DisableSnapshotCopy", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToDisableSnapshotRequest)
                            .makeServiceCall(this::disableSnapshotCopy)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("modify-snapshot-copy-retention-period")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ModifyClusterSnapshotRetentionCopy", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToModifySnapshotCopyRetentionPeriodRequest)
                            .makeServiceCall(this::modifySnapshotCopyRetentionPeriod)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("enable-logging")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-EnableLogging", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToEnableLoggingRequest)
                            .makeServiceCall(this::enableLogging)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("disable-logging")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-DisableLogging", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToDisableLoggingRequest)
                            .makeServiceCall(this::disableLogging)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("rotate-encryption-key")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-RotateEncryptionKey", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToRotateEncryptionKeyRequest)
                            .makeServiceCall(this::rotateEncryptionKey)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("resize-cluster")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ResizeCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToResizeClusterRequest)
                            .makeServiceCall(this::resizeCluster)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("cancel-resize")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-CancelResize", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToCancelResizeClusterRequest)
                            .makeServiceCall(this::cancelResize)
                            //.stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> {
                if(model.getRedshiftCommand().equals("modify-usage-limit")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ModifyUsageLimit", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToModifyUsageLimitRequest)
                            .makeServiceCall(this::modifyUsageLimit)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })

            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyClusterResponse updateCluster(
            final ModifyClusterRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyCluster);
        } catch (final InvalidClusterStateException | InvalidClusterSecurityGroupStateException | UnauthorizedOperationException
                | UnsupportedOptionException | LimitExceededException | InvalidElasticIpException | InvalidClusterTrackException | InvalidRetentionPeriodException
                | DependentServiceRequestThrottlingException | ClusterSubnetQuotaExceededException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterNotFoundException | ClusterSecurityGroupNotFoundException | ClusterParameterGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyRequest.toString(), e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyClusterIamRolesResponse updateIAMRoles(
            final ModifyClusterIamRolesRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterIamRolesResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyClusterIamRoles);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyRequest.toString(), e);
        }

        logger.log(String.format("%s IAM Roles successfully updated.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyClusterDbRevisionResponse modifyClusterDbRevision(
            final ModifyClusterDbRevisionRequest modifyClusterDbRevisionRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterDbRevisionResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyClusterDbRevisionRequest, proxyClient.client()::modifyClusterDbRevision);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifyClusterDbRevisionRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyClusterDbRevisionRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyClusterDbRevisionRequest.toString(), e);
        }

        logger.log(String.format("%s Update Cluster DB Revision.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyClusterMaintenanceResponse modifyClusterMaintenance(
            final ModifyClusterMaintenanceRequest modifyClusterMaintenanceRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterMaintenanceResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyClusterMaintenanceRequest, proxyClient.client()::modifyClusterMaintenance);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifyClusterMaintenanceRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyClusterMaintenanceRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyClusterMaintenanceRequest.toString(), e);
        }

        logger.log(String.format("%s Update Cluster Maintenance.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private EnableSnapshotCopyResponse enableSnapshotCopy(
            final EnableSnapshotCopyRequest enableSnapshotCopyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        EnableSnapshotCopyResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(enableSnapshotCopyRequest, proxyClient.client()::enableSnapshotCopy);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(enableSnapshotCopyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, enableSnapshotCopyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(enableSnapshotCopyRequest.toString(), e);
        }

        logger.log(String.format("%s Enable Cluster Snapshot Copy.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DisableSnapshotCopyResponse disableSnapshotCopy(
            final DisableSnapshotCopyRequest enableSnapshotCopyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DisableSnapshotCopyResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(enableSnapshotCopyRequest, proxyClient.client()::disableSnapshotCopy);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(enableSnapshotCopyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, enableSnapshotCopyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(enableSnapshotCopyRequest.toString(), e);
        }

        logger.log(String.format("%s Disable Cluster Snapshot Copy.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifySnapshotCopyRetentionPeriodResponse modifySnapshotCopyRetentionPeriod(
            final ModifySnapshotCopyRetentionPeriodRequest modifySnapshotCopyRetentionPeriodRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifySnapshotCopyRetentionPeriodResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifySnapshotCopyRetentionPeriodRequest, proxyClient.client()::modifySnapshotCopyRetentionPeriod);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifySnapshotCopyRetentionPeriodRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifySnapshotCopyRetentionPeriodRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifySnapshotCopyRetentionPeriodRequest.toString(), e);
        }

        logger.log(String.format("%s Modify Cluster Snapshot Copy.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private EnableLoggingResponse enableLogging(
            final EnableLoggingRequest enableLoggingRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        EnableLoggingResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(enableLoggingRequest, proxyClient.client()::enableLogging);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(enableLoggingRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, enableLoggingRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(enableLoggingRequest.toString(), e);
        }

        logger.log(String.format("%s Enable Logging.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private DisableLoggingResponse disableLogging(
            final DisableLoggingRequest disableLoggingRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DisableLoggingResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(disableLoggingRequest, proxyClient.client()::disableLogging);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(disableLoggingRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, disableLoggingRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(disableLoggingRequest.toString(), e);
        }

        logger.log(String.format("%s Disable Logging.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private RotateEncryptionKeyResponse rotateEncryptionKey(
            final RotateEncryptionKeyRequest rotateEncryptionKeyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RotateEncryptionKeyResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(rotateEncryptionKeyRequest, proxyClient.client()::rotateEncryptionKey);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(rotateEncryptionKeyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, rotateEncryptionKeyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(rotateEncryptionKeyRequest.toString(), e);
        }

        logger.log(String.format("%s Rotate Encryption Key.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ResizeClusterResponse resizeCluster(
            final ResizeClusterRequest resizeClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ResizeClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(resizeClusterRequest, proxyClient.client()::resizeCluster);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(resizeClusterRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resizeClusterRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(resizeClusterRequest.toString(), e);
        }

        logger.log(String.format("%s Resize Cluster.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private CancelResizeResponse cancelResize(
            final CancelResizeRequest cancelResizeRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CancelResizeResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(cancelResizeRequest, proxyClient.client()::cancelResize);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(cancelResizeRequest.toString(), e);
        } catch (final ClusterNotFoundException | ResizeNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, cancelResizeRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(cancelResizeRequest.toString(), e);
        }

        logger.log(String.format("%s Cancel Resize.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyUsageLimitResponse modifyUsageLimit(
            final  ModifyUsageLimitRequest modifyUsageLimitRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyUsageLimitResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyUsageLimitRequest, proxyClient.client()::modifyUsageLimit);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifyUsageLimitRequest.toString(), e);
        } catch (final UsageLimitNotFoundException | UsageLimitAlreadyExistsException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyUsageLimitRequest.usageLimitId());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyUsageLimitRequest.toString(), e);
        }

        logger.log(String.format("%s Modify Usage Limit.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private RebootClusterResponse rebootCluster(
            final RebootClusterRequest rebootClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RebootClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(rebootClusterRequest, proxyClient.client()::rebootCluster);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(rebootClusterRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, rebootClusterRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(rebootClusterRequest.toString(), e);
        }

        logger.log(String.format("%s Cluster Reboot.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }


    private PauseClusterResponse pauseCluster(
            final PauseClusterRequest pauseClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        PauseClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(pauseClusterRequest, proxyClient.client()::pauseCluster);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(pauseClusterRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, pauseClusterRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(pauseClusterRequest.toString(), e);
        }

        logger.log(String.format("%s Cluster Pause.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ResumeClusterResponse resumeCluster(
            final ResumeClusterRequest resumeClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ResumeClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(resumeClusterRequest, proxyClient.client()::resumeCluster);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(resumeClusterRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resumeClusterRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(resumeClusterRequest.toString(), e);
        }

        logger.log(String.format("%s Cluster Resume.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private boolean issueModifyClusterRequest(ResourceModel model) {
        return model.getNodeType() != null || model.getNumberOfNodes() != null || model.getNewClusterIdentifier() != null ||
                model.getAllowVersionUpgrade() != null || model.getAutomatedSnapshotRetentionPeriod() != null ||
                model.getClusterParameterGroupName() != null || model.getClusterType() != null || model.getClusterVersion() != null ||
                model.getElasticIp() != null || model.getEncrypted() != null || model.getEnhancedVpcRouting() != null ||
                model.getHsmClientCertificateIdentifier() != null || model.getHsmConfigurationIdentifier() != null || model.getMasterUserPassword() != null ||
                model.getKmsKeyId() != null || model.getMaintenanceTrackName() != null || model.getManualSnapshotRetentionPeriod() != null ||
                model.getPreferredMaintenanceWindow() != null || model.getPubliclyAccessible() != null || model.getClusterSecurityGroups() != null ||
                model.getVpcSecurityGroupIds() != null;
    }
}
