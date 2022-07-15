package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;;
import com.amazonaws.util.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.UnsupportedOperationException;
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

import java.util.List;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;
    private final String RESOURCE_NAME_PREFIX = "arn:aws:redshift:";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        if (!callbackContext.getClusterExistsCheck()) {
            boolean clusterExists = doesClusterExist(proxyClient, model, model.getClusterIdentifier());
            callbackContext.setClusterExistsCheck(true);
                if(!clusterExists) {
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.FAILED)
                            .errorCode(HandlerErrorCode.NotFound)
                            .message(String.format("Cluster %s Not Found %s", model.getClusterIdentifier(),HandlerErrorCode.NotFound.getMessage()))
                            .build();
            }
        }

        //Redshift is Driftable
        if(request.getDriftable() != null && request.getDriftable().equals(true)) {
            logger.log(String.format("%s %s is Drifted", ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
            if(isRebootRequired(model, proxyClient)) {
                return proxy.initiate("AWS-Redshift-Cluster::RebootCluster", proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToRebootClusterRequest)
                        .makeServiceCall(this::rebootCluster)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress();
            }
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> {
                    if (model.getResourceAction() != null && RESUME_CLUSTER.equals(model.getResourceAction()) &&
                            PAUSE_CLUSTER.equals(request.getPreviousResourceState().getResourceAction())) {
                        return proxy.initiate("AWS-Redshift-Cluster::ResumeCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToResumeClusterRequest)
                                .makeServiceCall(this::resumeCluster)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    List<List<Tag>> updateTags = updateTags(request.getPreviousResourceState().getTags(), model.getTags());

                    String resourceName = RESOURCE_NAME_PREFIX + request.getRegion() + ":" + request.getAwsAccountId() +
                            ":cluster:" + model.getClusterIdentifier();

                    if (!CollectionUtils.isNullOrEmpty(updateTags) && !CollectionUtils.isNullOrEmpty(updateTags.get(DELETE_TAGS_INDEX))) {
                        progress = proxy.initiate("AWS-Redshift-Cluster::DeleteTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest((deleteTagsRequest) -> Translator.translateToDeleteTagsRequest(model, updateTags.get(DELETE_TAGS_INDEX), resourceName))
                                .makeServiceCall(this::deleteTags)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }

                    if (!CollectionUtils.isNullOrEmpty(updateTags) && !CollectionUtils.isNullOrEmpty(updateTags.get(CREATE_TAGS_INDEX))) {
                        progress = proxy.initiate("AWS-Redshift-Cluster::CreateTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest((createTagsRequest) -> Translator.translateToCreateTagsRequest(model, updateTags.get(CREATE_TAGS_INDEX), resourceName))
                                .makeServiceCall(this::createTags)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }

                    return progress;
                })

                .then(progress -> {
                    List<List<String>> iamRolesForUpdate = iamRoleUpdate(request.getPreviousResourceState().getIamRoles(), model.getIamRoles());
                    if ((!CollectionUtils.isNullOrEmpty(iamRolesForUpdate)) && (!CollectionUtils.isNullOrEmpty(iamRolesForUpdate.get(ADD_IAM_ROLES_INDEX)) || !CollectionUtils.isNullOrEmpty(iamRolesForUpdate.get(DELETE_IAM_ROLES_INDEX)))) {
                        return proxy.initiate("AWS-Redshift-Cluster::UpdateClusterIAMRoles", proxyClient, model, callbackContext)
                                .translateToServiceRequest((iamRolesModifyRequest) -> Translator.translateToUpdateIAMRolesRequest(model, iamRolesForUpdate))
                                .makeServiceCall(this::updateIAMRoles)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if (model.getLoggingProperties() == null && isLoggingEnabled(proxyClient, model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::DisableLogging", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDisableLoggingRequest)
                                .makeServiceCall(this::disableLogging)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    } else if (model.getLoggingProperties() != null && (ObjectUtils.notEqual(model.getLoggingProperties(), request.getPreviousResourceState().getLoggingProperties()))) {
                        return proxy.initiate("AWS-Redshift-Cluster::EnableLogging", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToEnableLoggingRequest)
                                .makeServiceCall(this::enableLogging)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if ((ObjectUtils.allNotNull(model.getSnapshotCopyRetentionPeriod()) && issueModifySnapshotCopyRetentionPeriod(request.getPreviousResourceState(), model)) &&
                            isCrossRegionCopyEnabled(proxyClient, model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::ModifySnapshotCopyRetentionPeriod", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToModifySnapshotCopyRetentionPeriodRequest)
                                .makeServiceCall(this::modifySnapshotCopyRetentionPeriod)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if (model.getDestinationRegion() == null && ObjectUtils.anyNotNull(request.getPreviousResourceState().getDestinationRegion())
                            && isCrossRegionCopyEnabled(proxyClient, model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::DisableSnapshotCopy", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDisableSnapshotRequest)
                                .makeServiceCall(this::disableSnapshotCopy)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();

                    } else if (model.getDestinationRegion() != null) {
                        if (!isCrossRegionCopyEnabled(proxyClient, model)) {
                            return proxy.initiate("AWS-Redshift-Cluster::EnableSnapshotCopy", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(Translator::translateToEnableSnapshotRequest)
                                    .makeServiceCall(this::enableSnapshotCopy)
                                    .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                    .progress();
                        } else if (isCrossRegionCopyEnabled(proxyClient, model) &&
                                !model.getDestinationRegion().equals(destinationRegionForCrossRegionCopy(proxyClient, model))) {
                            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                                    .status(OperationStatus.FAILED)
                                    .errorCode(HandlerErrorCode.InvalidRequest)
                                    .message(String.format("Snapshot Copy is already enabled on Cluster %s. Invalid Request  %s", model.getClusterIdentifier(),HandlerErrorCode.InvalidRequest.getMessage()))
                                    .build();
                        }
                    }
                    return progress;
                })

                .then(progress -> {
                    if (issueModifyClusterMaintenanceRequest(request.getPreviousResourceState(), model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::ModifyClusterMaintenance", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator:: translateToModifyClusterMaintenanceRequest)
                                .makeServiceCall(this::modifyClusterMaintenance)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRevisionTarget() != null && !request.getPreviousResourceState().getRevisionTarget().equals(model.getRevisionTarget())) {
                        return proxy.initiate("AWS-Redshift-Cluster::ModifyClusterDbRevision", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToModifyClusterDbRevisionRequest)
                                .makeServiceCall(this::modifyClusterDbRevision)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterPatched(_client, _model, _context))
                                .done((_request, _response, _client, _model, _context) -> {
                                    if(!callbackContext.getCallbackAfterClusterMaintenance()) {
                                        logger.log(String.format("Update Cluster Db Revision done. %s %s stabilized and available.",ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
                                        callbackContext.setCallbackAfterClusterMaintenance(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Modify Cluster DbRevision.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })

                .then(progress -> {
                    if (model.getAquaConfigurationStatus() != null && !model.getAquaConfigurationStatus().equals(request.getPreviousResourceState().getAquaConfigurationStatus())) {
                        return proxy.initiate("AWS-Redshift-Cluster::ModifyAQUAConfiguration", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator:: translateToModifyAquaConfigurationRequest)
                                .makeServiceCall(this::modifyAquaConfiguration)
                                .stabilize((_request, _response, _client, _model, _context) -> isAquaConfigurationStatusApplied(_client, _model, _context))
                                .done((_request, _response, _client, _model, _context) -> {
                                    if(!callbackContext.getCallbackAfterAquaModify()) {
                                        logger.log(String.format("Update Aqua Configuration done. %s %s stabilized and available.",ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
                                        callbackContext.setCallbackAfterAquaModify(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Modify Aqua Configuration.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })

                .then(progress -> {
                    if (issueResizeClusterRequest(request.getPreviousResourceState(), model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::ResizeCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator:: translateToResizeClusterRequest)
                                .backoffDelay(BACKOFF_STRATEGY)
                                .makeServiceCall(this::resizeCluster)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done((_request, _response, _client, _model, _context) -> {
                                    logger.log(String.format("Resize Cluster complete. %s %s stabilized and available.",ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
                                    if(!callbackContext.getCallBackAfterResize()) {
                                        callbackContext.setCallBackAfterResize(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Resize Cluster.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })

                .then(progress -> {
                    if (issueModifyClusterRequest(request.getPreviousResourceState(), model)) {
                        return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest((modifyClusterRequest) -> Translator.translateToUpdateRequest(model, request.getPreviousResourceState()))
                                .backoffDelay(BACKOFF_STRATEGY)
                                .makeServiceCall(this::updateCluster)
                                .stabilize((_request, _response, _client, _model, _context) -> stabilizeCluster(_client, _model, _context, request))
                                .done((_request, _response, _client, _model, _context) -> {
                                    logger.log(String.format("Modify Cluster complete. %s %s stabilized and available.",ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
                                    if(!callbackContext.getCallBackForReboot()) {
                                        callbackContext.setCallBackForReboot(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Modify Cluster.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })

                .then(progress -> {
                    if ((issueModifyClusterRequest(request.getPreviousResourceState(), model) && isRebootRequired(model, proxyClient)) || isAQUAStatusApplying(model, proxyClient)){
                        return proxy.initiate("AWS-Redshift-Cluster::RebootCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToRebootClusterRequest)
                                .makeServiceCall(this::rebootCluster)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRotateEncryptionKey() != null && model.getRotateEncryptionKey()) {
                        return proxy.initiate("AWS-Redshift-Cluster::RotateEncryptionKey", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToRotateEncryptionKeyRequest)
                                .makeServiceCall(this::rotateEncryptionKey)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })

                .then(progress -> {
                    if (model.getResourceAction() != null && PAUSE_CLUSTER.equals(model.getResourceAction())) {
                        return proxy.initiate("AWS-Redshift-Cluster::PauseCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToPauseClusterRequest)
                                .makeServiceCall(this::pauseCluster)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterPaused(_client, _model, _context))
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
            logger.log(String.format("%s %s modifyCluster.", ResourceModel.TYPE_NAME,
                    modifyRequest.clusterIdentifier()));
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyCluster);
        } catch (final InvalidClusterStateException | InvalidClusterSecurityGroupStateException | UnauthorizedOperationException |
                UnsupportedOptionException | LimitExceededException | InvalidElasticIpException | InvalidClusterTrackException |
                DependentServiceRequestThrottlingException | ClusterSubnetQuotaExceededException | NumberOfNodesQuotaExceededException |
                NumberOfNodesPerClusterLimitExceededException | InsufficientClusterCapacityException | HsmClientCertificateNotFoundException | HsmConfigurationNotFoundException |
                ClusterAlreadyExistsException | TableLimitExceededException | InvalidRetentionPeriodException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ClusterNotFoundException | ClusterSecurityGroupNotFoundException |
                ClusterParameterGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s modify cluster issued.", ResourceModel.TYPE_NAME,
                modifyRequest.clusterIdentifier()));

        return awsResponse;
    }

    private ModifyClusterIamRolesResponse updateIAMRoles(
            final ModifyClusterIamRolesRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterIamRolesResponse awsResponse = null;

        try {
            logger.log(String.format("%s %s modifyClusterIamRoles.", ResourceModel.TYPE_NAME,
                    modifyRequest.clusterIdentifier()));
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyClusterIamRoles);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s modify IAM Roles issued.", ResourceModel.TYPE_NAME,
                modifyRequest.clusterIdentifier()));

        return awsResponse;
    }

    private ResizeClusterResponse resizeCluster(
            final ResizeClusterRequest resizeClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ResizeClusterResponse resizeClusterResponse = null;

        try {
            logger.log(String.format("%s %s resizeCluster.", ResourceModel.TYPE_NAME,
                    resizeClusterRequest.clusterIdentifier()));
            resizeClusterResponse = proxyClient.injectCredentialsAndInvokeV2(resizeClusterRequest, proxyClient.client()::resizeCluster);
        } catch (final InvalidClusterStateException | UnauthorizedOperationException |
                UnsupportedOptionException | LimitExceededException | NumberOfNodesQuotaExceededException |
                NumberOfNodesPerClusterLimitExceededException | InsufficientClusterCapacityException |
                UnsupportedOperationException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resizeClusterRequest.clusterIdentifier(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s resize cluster issued.", ResourceModel.TYPE_NAME,
                resizeClusterRequest.clusterIdentifier()));

        return resizeClusterResponse;
    }

    private ModifyAquaConfigurationResponse modifyAquaConfiguration(
            final ModifyAquaConfigurationRequest modifyAquaConfigurationRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyAquaConfigurationResponse modifyAquaConfigurationResponse = null;
        try {
            logger.log(String.format("%s %s modifyAquaConfiguration.", ResourceModel.TYPE_NAME,
                    modifyAquaConfigurationRequest.clusterIdentifier()));
            modifyAquaConfigurationResponse = proxyClient.injectCredentialsAndInvokeV2(modifyAquaConfigurationRequest, proxyClient.client()::modifyAquaConfiguration);
        } catch (final InvalidClusterStateException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyAquaConfigurationRequest.clusterIdentifier(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s modifyAquaConfiguration issued.", ResourceModel.TYPE_NAME,
                modifyAquaConfigurationRequest.clusterIdentifier()));

        return modifyAquaConfigurationResponse;
    }

    private ModifyClusterMaintenanceResponse modifyClusterMaintenance(
            final ModifyClusterMaintenanceRequest modifyClusterMaintenanceRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterMaintenanceResponse modifyClusterMaintenanceResponse = null;

        try {
            logger.log(String.format("%s %s modifyClusterMaintenance.", ResourceModel.TYPE_NAME,
                    modifyClusterMaintenanceRequest.clusterIdentifier()));
            modifyClusterMaintenanceResponse = proxyClient.injectCredentialsAndInvokeV2(modifyClusterMaintenanceRequest, proxyClient.client()::modifyClusterMaintenance);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(modifyClusterMaintenanceRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyClusterMaintenanceRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(modifyClusterMaintenanceRequest.toString(), e);
        }

        logger.log(String.format("%s %s modifyClusterMaintenance issued.", ResourceModel.TYPE_NAME,
                modifyClusterMaintenanceRequest.clusterIdentifier()));

        return modifyClusterMaintenanceResponse;
    }

    private ModifyClusterDbRevisionResponse modifyClusterDbRevision(
            final ModifyClusterDbRevisionRequest modifyClusterDbRevisionRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterDbRevisionResponse modifyClusterDbRevisionResponse = null;

        try {
            logger.log(String.format("%s %s modifyClusterDbRevisionRequest.", ResourceModel.TYPE_NAME,
                    modifyClusterDbRevisionRequest.clusterIdentifier()));
            modifyClusterDbRevisionResponse = proxyClient.injectCredentialsAndInvokeV2(modifyClusterDbRevisionRequest, proxyClient.client()::modifyClusterDbRevision);
        } catch (final InvalidClusterStateException | ClusterOnLatestRevisionException e ) {
            throw new CfnInvalidRequestException(modifyClusterDbRevisionRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyClusterDbRevisionRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(modifyClusterDbRevisionRequest.toString(), e);
        }

        logger.log(String.format("%s %s modifyClusterDbRevisionRequest issued.", ResourceModel.TYPE_NAME,
                modifyClusterDbRevisionRequest.clusterIdentifier()));

        return modifyClusterDbRevisionResponse;
    }

    private CreateTagsResponse createTags(
            final CreateTagsRequest createTagsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateTagsResponse createTagsResponse = null;

        try {
            logger.log(String.format("createTags for %s", createTagsRequest.resourceName()));
            createTagsResponse = proxyClient.injectCredentialsAndInvokeV2(createTagsRequest, proxyClient.client()::createTags);
        } catch (final InvalidClusterStateException | TagLimitExceededException | InvalidTagException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, createTagsRequest.resourceName(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s create tags for resource issued.", ResourceModel.TYPE_NAME,
                createTagsRequest.resourceName()));

        return createTagsResponse;
    }

    private DeleteTagsResponse deleteTags(
            final DeleteTagsRequest deleteTagsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteTagsResponse deleteTagsResponse = null;

        try {
            logger.log(String.format("deleteTags for %s", deleteTagsRequest.resourceName()));
            deleteTagsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteTagsRequest, proxyClient.client()::deleteTags);
        } catch (final InvalidClusterStateException | TagLimitExceededException | InvalidTagException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteTagsRequest.resourceName(), e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s delete tags for resource issued.", ResourceModel.TYPE_NAME,
                deleteTagsRequest.resourceName()));

        return deleteTagsResponse;
    }

    private DisableLoggingResponse disableLogging(
            final DisableLoggingRequest disableLoggingRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DisableLoggingResponse disableLoggingResponse = null;

        try {
            logger.log(String.format("%s %s disableLogging.", ResourceModel.TYPE_NAME,
                    disableLoggingRequest.clusterIdentifier()));
            disableLoggingResponse = proxyClient.injectCredentialsAndInvokeV2(disableLoggingRequest, proxyClient.client()::disableLogging);
        } catch (final ClusterNotFoundException  e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }
        logger.log(String.format("%s %s disable logging properties issued.", ResourceModel.TYPE_NAME,
                disableLoggingRequest.clusterIdentifier()));

        return disableLoggingResponse;
    }

    private EnableLoggingResponse enableLogging(
            final EnableLoggingRequest enableLoggingRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        EnableLoggingResponse enableLoggingResponse = null;

        try {
            logger.log(String.format("%s %s enableLogging.", ResourceModel.TYPE_NAME,
                    enableLoggingRequest.clusterIdentifier()));
            enableLoggingResponse = proxyClient.injectCredentialsAndInvokeV2(enableLoggingRequest, proxyClient.client()::enableLogging);
        } catch (final ClusterNotFoundException | BucketNotFoundException | InsufficientS3BucketPolicyException
                | InvalidS3KeyPrefixException | InvalidS3BucketNameException | InvalidClusterStateException  e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }
        logger.log(String.format("%s %s enable logging properties issued.",
                ResourceModel.TYPE_NAME, enableLoggingRequest.clusterIdentifier()));

        return enableLoggingResponse;
    }

    private EnableSnapshotCopyResponse enableSnapshotCopy(
            final EnableSnapshotCopyRequest enableSnapshotCopyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        EnableSnapshotCopyResponse enableSnapshotCopyResponse = null;
        try {
            enableSnapshotCopyResponse = proxyClient.injectCredentialsAndInvokeV2(enableSnapshotCopyRequest, proxyClient.client()::enableSnapshotCopy);
        } catch (final InvalidClusterStateException | IncompatibleOrderableOptionsException | CopyToRegionDisabledException
                | SnapshotCopyAlreadyEnabledException | UnknownSnapshotCopyRegionException | UnauthorizedOperationException |
                SnapshotCopyGrantNotFoundException | LimitExceededException | DependentServiceRequestThrottlingException
                | InvalidRetentionPeriodException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, enableSnapshotCopyRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("Enable Cluster Snapshot Copy issued for %s %s in destination region %s.", ResourceModel.TYPE_NAME,
                enableSnapshotCopyRequest.clusterIdentifier(), enableSnapshotCopyRequest.destinationRegion()));

        return enableSnapshotCopyResponse;
    }

    private DisableSnapshotCopyResponse disableSnapshotCopy(
            final DisableSnapshotCopyRequest disableSnapshotCopyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DisableSnapshotCopyResponse disableSnapshotCopyResponse = null;

        try {
            disableSnapshotCopyResponse = proxyClient.injectCredentialsAndInvokeV2(disableSnapshotCopyRequest, proxyClient.client()::disableSnapshotCopy);
        } catch (final InvalidClusterStateException | SnapshotCopyAlreadyDisabledException | UnauthorizedOperationException e ) {
            throw new CfnInvalidRequestException(disableSnapshotCopyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, disableSnapshotCopyRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(disableSnapshotCopyRequest.toString(), e);
        }

        logger.log(String.format("Disable Cluster Snapshot Copy issued for %s %s .", ResourceModel.TYPE_NAME,
                disableSnapshotCopyRequest.clusterIdentifier()));

        return disableSnapshotCopyResponse;
    }

    private ModifySnapshotCopyRetentionPeriodResponse modifySnapshotCopyRetentionPeriod(
            final ModifySnapshotCopyRetentionPeriodRequest modifySnapshotCopyRetentionPeriodRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifySnapshotCopyRetentionPeriodResponse modifySnapshotCopyRetentionPeriodResponse = null;

        try {
            modifySnapshotCopyRetentionPeriodResponse = proxyClient.injectCredentialsAndInvokeV2(modifySnapshotCopyRetentionPeriodRequest, proxyClient.client()::modifySnapshotCopyRetentionPeriod);
        } catch (final InvalidClusterStateException | SnapshotCopyDisabledException | UnauthorizedOperationException | InvalidRetentionPeriodException e ) {
            throw new CfnInvalidRequestException(modifySnapshotCopyRetentionPeriodRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifySnapshotCopyRetentionPeriodRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(modifySnapshotCopyRetentionPeriodRequest.toString(), e);
        }

        logger.log(String.format("Modify Snapshot Copy Retention Period issued for %s %s .", ResourceModel.TYPE_NAME,
                modifySnapshotCopyRetentionPeriodRequest.clusterIdentifier()));

        return modifySnapshotCopyRetentionPeriodResponse;
    }

    private RebootClusterResponse rebootCluster (
            final RebootClusterRequest rebootClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RebootClusterResponse rebootClusterResponse = null;
        try {
            logger.log(String.format("%s %s rebootCluster.", ResourceModel.TYPE_NAME,
                    rebootClusterRequest.clusterIdentifier()));
            rebootClusterResponse = proxyClient.injectCredentialsAndInvokeV2(rebootClusterRequest, proxyClient.client()::rebootCluster);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, rebootClusterRequest.clusterIdentifier(), e);
        } catch (final InvalidClusterStateException e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s Reboot Cluster issued.", ResourceModel.TYPE_NAME,
                rebootClusterRequest.clusterIdentifier()));
        return rebootClusterResponse;
    }

    private ResumeClusterResponse resumeCluster (
            final ResumeClusterRequest resumeClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ResumeClusterResponse resumeClusterResponse = null;
        try {
            logger.log(String.format("%s %s resumeCluster.", ResourceModel.TYPE_NAME,
                    resumeClusterRequest.clusterIdentifier()));
            resumeClusterResponse = proxyClient.injectCredentialsAndInvokeV2(resumeClusterRequest, proxyClient.client()::resumeCluster);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resumeClusterRequest.clusterIdentifier(), e);
        } catch (final InvalidClusterStateException | InsufficientClusterCapacityException e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s Resume Cluster issued.", ResourceModel.TYPE_NAME,
                resumeClusterRequest.clusterIdentifier()));
        return resumeClusterResponse;
    }

    private PauseClusterResponse pauseCluster (
            final PauseClusterRequest pauseClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        PauseClusterResponse pauseClusterResponse = null;
        try {
            logger.log(String.format("%s %s pauseCluster.", ResourceModel.TYPE_NAME,
                    pauseClusterRequest.clusterIdentifier()));
            pauseClusterResponse = proxyClient.injectCredentialsAndInvokeV2(pauseClusterRequest, proxyClient.client()::pauseCluster);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, pauseClusterRequest.clusterIdentifier(), e);
        } catch (final InvalidClusterStateException e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s Pause Cluster issued.", ResourceModel.TYPE_NAME,
                pauseClusterRequest.clusterIdentifier()));
        return pauseClusterResponse;
    }

    private RotateEncryptionKeyResponse rotateEncryptionKey(
            final RotateEncryptionKeyRequest rotateEncryptionKeyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RotateEncryptionKeyResponse rotateEncryptionKeyResponse = null;

        try {
            logger.log(String.format("%s %s RotateEncryptionKey.", ResourceModel.TYPE_NAME,
                    rotateEncryptionKeyRequest.clusterIdentifier()));
            rotateEncryptionKeyResponse = proxyClient.injectCredentialsAndInvokeV2(rotateEncryptionKeyRequest, proxyClient.client()::rotateEncryptionKey);
        } catch (final InvalidClusterStateException | DependentServiceRequestThrottlingException e ) {
            throw new CfnInvalidRequestException(rotateEncryptionKeyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, rotateEncryptionKeyRequest.clusterIdentifier());
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(rotateEncryptionKeyRequest.toString(), e);
        }

        logger.log(String.format("%s %s RotateEncryptionKey issued.", ResourceModel.TYPE_NAME,
                rotateEncryptionKeyRequest.clusterIdentifier()));

        return rotateEncryptionKeyResponse;
    }
}
