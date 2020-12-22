package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CancelResizeResponse;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeResizeRequest;
import software.amazon.awssdk.services.redshift.model.DescribeResizeResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTableRestoreStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTableRestoreStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeUsageLimitsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeUsageLimitsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRestoreException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResizeNotFoundException;
import software.amazon.awssdk.services.redshift.model.UsageLimitNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
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
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("describe-cluster-db-revisions")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeCluster-DB-Revisions", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeClusterDBRevisionsRequest)
                                .makeServiceCall(this::readDescribeClusterDbRevisionsResponse)
                                .done(this::constructResourceModelFromDescribeClusterDbRevisionsResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRedshiftCommand() != null &&
                            (model.getRedshiftCommand().equals("restore-table-from-cluster-snapshot") || model.getRedshiftCommand().equals("describe-table-restore-status"))) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeTableRestoreStatus", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToTableRestoreStatusRequest)
                                .makeServiceCall(this::tableRestoreStatus)
                                .done(this::constructResourceModelFromTableRestoreStatus);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getBucketName() != null || (model.getRedshiftCommand() != null && model.getRedshiftCommand().contains("logging"))) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeLogging", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeLoggingRequest)
                                .makeServiceCall(this::describeLogging)
                                .done(this::constructResourceModelFromDescribeLoggingResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().contains("limit")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeUsageLimit", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeUsageLimitRequest)
                                .makeServiceCall(this::describeUsageLimit)
                                .done(this::constructResourceModelFromDescribeUsageLimitResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("cancel-resize")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeResize", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeResizeRequest)
                                .makeServiceCall(this::describeResize)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .done(this::constructResourceModelFromDescribeResizeResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("describe-resize")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeResize", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeResizeRequest)
                                .makeServiceCall(this::describeResize)
                                .done(this::constructResourceModelFromDescribeResizeResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                    if(model.getRedshiftCommand() != null && model.getRedshiftCommand().equals("describe-tags")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeTagsRequest)
                                .makeServiceCall(this::describeTags)
                                .done(this::constructResourceModelFromDescribeTagsResponse);
                    }
                    return progress;
                })

                .then(progress -> {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToReadRequest)
                                .makeServiceCall(this::readCluster)
                                .done(this::constructResourceModelFromResponse);
                });

    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeClustersResponse readCluster(
            final DescribeClustersRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClustersResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DescribeClusterDbRevisionsResponse readDescribeClusterDbRevisionsResponse(
            final DescribeClusterDbRevisionsRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClusterDbRevisionsResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterDbRevisions);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DescribeTableRestoreStatusResponse tableRestoreStatus(
            final DescribeTableRestoreStatusRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeTableRestoreStatusResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeTableRestoreStatus);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException | InvalidRestoreException e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s Table Restore status read", ResourceModel.TYPE_NAME));
        return awsResponse;
    }


    private DescribeLoggingStatusResponse describeLogging(
            final DescribeLoggingStatusRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeLoggingStatusResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeLoggingStatus);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException | InvalidRestoreException e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s Logging Status read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DescribeUsageLimitsResponse describeUsageLimit(
            final  DescribeUsageLimitsRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeUsageLimitsResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeUsageLimits);
        } catch (final ClusterNotFoundException | UsageLimitNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException  e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s Describe Usage Limit.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DescribeResizeResponse describeResize(
            final DescribeResizeRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeResizeResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeResize);
        } catch (final ClusterNotFoundException | ResizeNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier());
        } catch (final InvalidClusterStateException  e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s Describe Resize", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private DescribeTagsResponse describeTags(
            final DescribeTagsRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeTagsResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeTags);
        } catch (final ResourceNotFoundException  e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.resourceName());
        } catch (final InvalidTagException e ) {
            throw new CfnInvalidRequestException(awsRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(awsRequest.toString(), e);
        } catch (final AwsServiceException e) { // ResourceNotFoundException
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s Describe Tags", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeClustersResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse));
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeClusterDbRevisionsResponse(
            final DescribeClusterDbRevisionsResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeClusterDbRevisionsResponse(awsResponse));
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromTableRestoreStatus(
            final DescribeTableRestoreStatusResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromTableRestoreStatus(awsResponse));
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeLoggingResponse(
            final DescribeLoggingStatusResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeLoggingResponse(awsResponse));
    }
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeUsageLimitResponse(
            final DescribeUsageLimitsResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeUsageLimitResponse(awsResponse));
    }
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeResizeResponse(
            final DescribeResizeResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeResizeResponse(awsResponse));
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeTagsResponse(
            final DescribeTagsResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeTagsResponse(awsResponse));
    }
}
