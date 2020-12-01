package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSnapshotsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSnapshotsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
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

        boolean clusterExists = isClusterAvailableForUpdate(proxyClient, model, model.getClusterIdentifier());
        if(!clusterExists) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(HandlerErrorCode.NotFound.getMessage())
                    .build();
        }

//        return proxy.initiate("AWS-Redshift-Cluster::Read", proxyClient, model, callbackContext)
//                .translateToServiceRequest(Translator::translateToReadRequest)
//                .makeServiceCall(this::readResource)
//                .done(this::constructResourceModelFromResponse);


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
                    if(model.getRedshiftCommand() != null && (model.getRedshiftCommand().equals("describe-cluster-snapshots") ||
                            model.getRedshiftCommand().equals("modify-cluster-snapshot"))) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeCluster-Snapshots", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDescribeClusterSnapshotRequest)
                                .makeServiceCall(this::readClusterSnapshot)
                                .done(this::constructResourceModelFromDescribeClusterSnapshotResponse);
                    }
                    return progress;
                })
                .then(progress -> {
                    //if(model.getRedshiftCommand() == null || model.getRedshiftCommand().equals("describe-cluster")) {
                        return proxy.initiate("AWS-Redshift-Cluster::DescribeCluster", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToReadRequest)
                                .makeServiceCall(this::readCluster)
                                .done(this::constructResourceModelFromResponse);
                    //}
                    //return progress;
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

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeClusterSnapshotsResponse readClusterSnapshot(
            final DescribeClusterSnapshotsRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClusterSnapshotsResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterSnapshots);
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
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadDescribeClusterDbRevisionsResponse(awsResponse));
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeClusterSnapshotResponse(
            final DescribeClusterSnapshotsResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadDescribeClusterSnapshotResponse(awsResponse));
    }
}
