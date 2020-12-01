package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterOnLatestRevisionException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSecurityGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterTrackException;
import software.amazon.awssdk.services.redshift.model.InvalidElasticIpException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.LimitExceededException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSnapshotResponse;
import software.amazon.awssdk.services.redshift.model.PauseClusterRequest;
import software.amazon.awssdk.services.redshift.model.PauseClusterResponse;
import software.amazon.awssdk.services.redshift.model.RebootClusterRequest;
import software.amazon.awssdk.services.redshift.model.RebootClusterResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResumeClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResumeClusterResponse;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.awssdk.services.redshift.model.UnsupportedOptionException;
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

        boolean clusterExists = isClusterAvailableForUpdate(proxyClient, model, model.getClusterIdentifier());
        if(!clusterExists) {
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
                            .makeServiceCall(this::updateResource)
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
                if(model.getRedshiftCommand().equals("modify-cluster-maintenance")) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster-ModifyClusterSnapshot", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToModifyClusterSnapshotRequest)
                            .makeServiceCall(this::modifyClusterSnapshot)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyClusterResponse updateResource(
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

        logger.log(String.format("%s Update cluster DB Revision.", ResourceModel.TYPE_NAME));

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

        logger.log(String.format("%s Update cluster DB Revision.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyClusterSnapshotResponse modifyClusterMaintenance(
            final ModifyClusterSnapshotRequest modifyClusterSnapshotRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterSnapshotResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyClusterSnapshotRequest, proxyClient.client()::modifyClusterSnapshot);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifyClusterSnapshotRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyClusterSnapshotRequest.snapshotIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyClusterSnapshotRequest.toString(), e);
        }

        logger.log(String.format("%s Update cluster DB Revision.", ResourceModel.TYPE_NAME));

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
