package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AccessToSnapshotDeniedException;
import software.amazon.awssdk.services.redshift.model.BucketNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.HsmClientCertificateNotFoundException;
import software.amazon.awssdk.services.redshift.model.HsmConfigurationNotFoundException;
import software.amazon.awssdk.services.redshift.model.InsufficientClusterCapacityException;
import software.amazon.awssdk.services.redshift.model.InsufficientS3BucketPolicyException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSnapshotStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterTrackException;
import software.amazon.awssdk.services.redshift.model.InvalidElasticIpException;
import software.amazon.awssdk.services.redshift.model.InvalidRestoreException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.InvalidS3BucketNameException;
import software.amazon.awssdk.services.redshift.model.InvalidS3KeyPrefixException;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.InvalidVpcNetworkStateException;
import software.amazon.awssdk.services.redshift.model.LimitExceededException;
import software.amazon.awssdk.services.redshift.model.NumberOfNodesPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.NumberOfNodesQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotResponse;
import software.amazon.awssdk.services.redshift.model.SnapshotScheduleNotFoundException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
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

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> {
                    if (!StringUtils.isNullOrEmpty(resourceModel.getSnapshotIdentifier())) {
                        return proxy.initiate("AWS-Redshift-Cluster::restoreFromClusterSnapshot", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest(Translator::translateToRestoreFromClusterSnapshotRequest)
                                .backoffDelay(CREATE_BACKOFF_STRATEGY)
                                .makeServiceCall(this::restoreFromClusterSnapshot)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done((_request, _response, _client, _model, _context) -> {
                                    if(!callbackContext.getCallbackAfterClusterRestore()) {
                                        logger.log(String.format("Cluster Restore done. %s %s stabilized and available.",ResourceModel.TYPE_NAME, resourceModel.getClusterIdentifier()));
                                        callbackContext.setCallbackAfterClusterRestore(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Cluster Restore.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                        });
                    }
                    return progress;
                })
                .then(progress -> {
                    if (StringUtils.isNullOrEmpty(resourceModel.getSnapshotIdentifier()) && !invalidCreateClusterRequest(resourceModel)) {
                        return proxy.initiate("AWS-Redshift-Cluster::createCluster", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .backoffDelay(CREATE_BACKOFF_STRATEGY)
                                .makeServiceCall(this::createClusterResource)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done((_request, _response, _client, _model, _context) -> {
                                    if(!callbackContext.getCallbackAfterClusterCreate()) {
                                        logger.log(String.format("Cluster Create done. %s %s stabilized and available.",ResourceModel.TYPE_NAME, resourceModel.getClusterIdentifier()));
                                        callbackContext.setCallbackAfterClusterCreate(true);
                                        logger.log ("Initiate a CallBack Delay of "+CALLBACK_DELAY_SECONDS+" seconds after Cluster Create.");
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })
                .then(progress -> {
                    if (resourceModel.getLoggingProperties() != null) {
                        return proxy.initiate("AWS-Redshift-Cluster::enableLogging", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest(Translator::translateToEnableLoggingRequest)
                                .makeServiceCall(this::enableLogging)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    return progress;
                })
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private RestoreFromClusterSnapshotResponse restoreFromClusterSnapshot(
            final RestoreFromClusterSnapshotRequest restoreFromClusterSnapshotRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RestoreFromClusterSnapshotResponse restoreFromClusterSnapshotResponse = null;

        try {
            logger.log(String.format("restoreFromClusterSnapshot for %s", restoreFromClusterSnapshotRequest.clusterIdentifier()));
            restoreFromClusterSnapshotResponse = proxyClient.injectCredentialsAndInvokeV2(restoreFromClusterSnapshotRequest,
                    proxyClient.client()::restoreFromClusterSnapshot);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, restoreFromClusterSnapshotRequest.clusterIdentifier());
        }  catch (final AccessToSnapshotDeniedException | InvalidClusterStateException | InvalidRetentionPeriodException
                | ClusterSnapshotNotFoundException | ClusterQuotaExceededException | InsufficientClusterCapacityException
                | InvalidClusterSnapshotStateException | InvalidRestoreException | NumberOfNodesQuotaExceededException
                | NumberOfNodesPerClusterLimitExceededException | InvalidVpcNetworkStateException | InvalidClusterSubnetGroupStateException
                | InvalidSubnetException | ClusterSubnetGroupNotFoundException | UnauthorizedOperationException
                | HsmClientCertificateNotFoundException | HsmConfigurationNotFoundException
                | InvalidElasticIpException | ClusterParameterGroupNotFoundException | ClusterSecurityGroupNotFoundException
                | LimitExceededException | DependentServiceRequestThrottlingException
                | InvalidClusterTrackException | SnapshotScheduleNotFoundException | TagLimitExceededException
                | InvalidTagException e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }
        logger.log(String.format("%s %s Restore Cluster from Snapshot issued.", ResourceModel.TYPE_NAME, restoreFromClusterSnapshotRequest.clusterIdentifier()));

        return restoreFromClusterSnapshotResponse;
    }

    private CreateClusterResponse createClusterResource(
            final CreateClusterRequest createRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterResponse createResponse = null;

        try {
            logger.log(String.format("createCluster for %s", createRequest.clusterIdentifier()));
            createResponse = proxyClient.injectCredentialsAndInvokeV2(createRequest, proxyClient.client()::createCluster);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException | InsufficientClusterCapacityException |
                ClusterParameterGroupNotFoundException | ClusterSecurityGroupNotFoundException | ClusterQuotaExceededException |
                NumberOfNodesQuotaExceededException | NumberOfNodesPerClusterLimitExceededException |
                ClusterSubnetGroupNotFoundException | InvalidVpcNetworkStateException | InvalidClusterSubnetGroupStateException |
                InvalidSubnetException | UnauthorizedOperationException | HsmClientCertificateNotFoundException |
                HsmConfigurationNotFoundException | InvalidElasticIpException | TagLimitExceededException | InvalidTagException |
                LimitExceededException | DependentServiceRequestThrottlingException | InvalidClusterTrackException |
                SnapshotScheduleNotFoundException  e) {
            throw new CfnInvalidRequestException(e);
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }
        logger.log(String.format("%s %s Create issued.", ResourceModel.TYPE_NAME, createRequest.clusterIdentifier()));

        return createResponse;
    }

    private EnableLoggingResponse enableLogging(
            final EnableLoggingRequest enableLoggingRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        EnableLoggingResponse enableLoggingResponse = null;

        try {
            logger.log(String.format("enableLogging for %s", enableLoggingRequest.clusterIdentifier()));
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

    private void prepareResourceModel(ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }
        final ResourceModel model = request.getDesiredResourceState();

        String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                ? "cluster-" + UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();

        if (StringUtils.isNullOrEmpty(model.getClusterIdentifier())) {
            logger.log(String.format("Setting Cluster Identifier as it is not found for %s", ResourceModel.TYPE_NAME));
            model.setClusterIdentifier(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_CLUSTER_IDENTIFIER_LENGTH
                    ).toLowerCase()
            );
            logger.log(String.format("Set Cluster Identifier for %s as %s", ResourceModel.TYPE_NAME, model.getClusterIdentifier()));
        }
    }
}
