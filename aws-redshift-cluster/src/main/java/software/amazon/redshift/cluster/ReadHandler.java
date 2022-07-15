package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRestoreException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
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
    private final String DESCRIBE_LOGGING_ERROR = "not authorized to perform: redshift:DescribeLoggingStatus";
    private final String DESCRIBE_LOGGING_ERROR_CODE = "403";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        boolean clusterExists = doesClusterExist(proxyClient, model, model.getClusterIdentifier());
        if(!clusterExists) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(String.format("Cluster %s Not Found %s", model.getClusterIdentifier(),HandlerErrorCode.NotFound.getMessage()))
                    .build();
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> {
                        progress = proxy.initiate("AWS-Redshift-Cluster::DescribeLogging", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToDescribeStatusLoggingRequest)
                            .makeServiceCall(this::describeLoggingStatus)
                            .done(enableLoggingResponse -> {
                                LoggingProperties loggingProperties = LoggingProperties.builder()
                                        .bucketName(enableLoggingResponse.bucketName())
                                        .s3KeyPrefix(enableLoggingResponse.s3KeyPrefix())
                                        .build();
                                callbackContext.setLoggingProperties(loggingProperties);
                                model.setLoggingProperties(loggingProperties);
                                return ProgressEvent.progress(model, callbackContext);
                            });
                    return progress;
                })

                .then(progress -> {
                     progress = proxy.initiate("AWS-Redshift-Cluster::DescribeCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToDescribeClusterRequest)
                            .makeServiceCall(this::describeCluster)
                            .done(this::constructResourceModelFromResponse);
                     progress.getResourceModel().setLoggingProperties(callbackContext.getLoggingProperties());
                     return  progress;
                });
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
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

    private DescribeLoggingStatusResponse describeLoggingStatus(
            final DescribeLoggingStatusRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeLoggingStatusResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeLoggingStatus);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier(), e);
        } catch (final InvalidClusterStateException | InvalidRestoreException e ) {
            throw new CfnInvalidRequestException(e);
        } catch (RedshiftException e) {
            if (e.awsErrorDetails().errorCode().equals(DESCRIBE_LOGGING_ERROR_CODE) &&
                    e.awsErrorDetails().errorMessage().contains(DESCRIBE_LOGGING_ERROR)) {
                logger.log(String.format("RedshiftException: User is not authorized to perform: redshift:DescribeLoggingStatus on resource %s",
                        e.getMessage()));
            } else {
                throw new CfnGeneralServiceException(e);
            }
        } catch (SdkClientException | AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s %s Logging Status read.", ResourceModel.TYPE_NAME, awsRequest.clusterIdentifier()));
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

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromDescribeLoggingResponse(
            final DescribeLoggingStatusResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromDescribeLoggingResponse(awsResponse));
    }
}
