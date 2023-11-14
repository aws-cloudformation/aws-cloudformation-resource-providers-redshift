package software.amazon.redshift.cluster;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnsupportedOperationException;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
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
                                        _model.setClusterNamespaceArn(_response.cluster().clusterNamespaceArn());
                                        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, _model);
                                    }
                                    resourceModel.setClusterNamespaceArn(_response.cluster().clusterNamespaceArn());
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
                .then(progress -> {
                    if (resourceModel.getClusterIdentifier() != null) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeCluster", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeClusterRequest)
                                .makeServiceCall(this::describeCluster)
                                .done((_request, _response, _client, _model, _context) -> {
                                    _model.setClusterNamespaceArn(Translator.translateFromReadResponse(_response).getClusterNamespaceArn());
                                    return ProgressEvent.progress(_model, callbackContext);
                                });
                    }
                    return progress;
                })
                .then(progress -> {
                    if (resourceModel.getClusterNamespaceArn() != null && resourceModel.getNamespaceResourcePolicy() != null) {
                        return proxy.initiate("AWS-Redshift-Cluster::putResourcePolicy", proxyClient, resourceModel, callbackContext)
                                .translateToServiceRequest(resourceModelRequest -> Translator.translateToPutResourcePolicy(resourceModelRequest, logger))
                                .makeServiceCall(this::putNamespaceResourcePolicy)
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

    private DescribeClustersResponse describeCluster (
        final DescribeClustersRequest awsRequest,
        final ProxyClient<RedshiftClient> proxyClient) {
            DescribeClustersResponse awsResponse = null;
            try {
                logger.log(String.format("%s %s describeClusters.", ResourceModel.TYPE_NAME,
                        awsRequest.clusterIdentifier()));
                awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
            } catch (final ClusterNotFoundException e) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier(), e);
            } catch (final InvalidTagException e) {
                throw new CfnInvalidRequestException(e);
            } catch (SdkClientException | AwsServiceException e) {
                throw new CfnGeneralServiceException(e);
            }

            logger.log(String.format("%s %s has successfully been read.", ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier()));
            return awsResponse;
        }

    private PutResourcePolicyResponse putNamespaceResourcePolicy(
        final PutResourcePolicyRequest putRequest,
        final ProxyClient<RedshiftClient> proxyClient) {
            PutResourcePolicyResponse putResponse = null;

            try {
                logger.log(String.format("%s %s putResourcePolicy.", ResourceModel.TYPE_NAME,
                        putRequest.resourceArn()));
                putResponse = proxyClient.injectCredentialsAndInvokeV2(putRequest, proxyClient.client()::putResourcePolicy);
            } catch (ResourceNotFoundException e){
                throw new CfnNotFoundException(e);
            } catch (InvalidPolicyException | UnsupportedOperationException | InvalidParameterValueException e) {
                throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
            } catch (SdkClientException | RedshiftException  e) {
                throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
            }

            logger.log(String.format("%s successfully put resource policy.", putRequest.resourceArn()));
            return putResponse;
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
