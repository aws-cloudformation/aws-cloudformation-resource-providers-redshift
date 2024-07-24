package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidPolicyException;
import software.amazon.awssdk.services.redshift.model.InvalidRestoreException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.ResourcePolicy;
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

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;
    private final String DESCRIBE_LOGGING_ERROR = "not authorized to perform: redshift:DescribeLoggingStatus";
    private final String DESCRIBE_LOGGING_ERROR_CODE = "403";
    private final String GET_RESOURCE_POLICY_ERROR = "not authorized to perform: redshift:GetResourcePolicy";
    private final Integer GET_RESOURCE_POLICY_ERR_STATUS_CODE = 403;
    private final String RESOURCE_POLICY_UNSUPPORTED_ERROR = "The resource policy feature isn't supported";
    private final Integer RESOURCE_POLICY_UNSUPPORTED_ERR_STATUS_CODE = 400;
    private boolean containsResourcePolicy = false;

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

        /*
        containsResourcePolicy will be true if NamespaceResourcePolicy property is included in the template.
        This attribute will be used to decide if "not authorized to perform: redshift:GetResourcePolicy" errors
        in Read handler should be suppressed or not.
         */
        containsResourcePolicy = model.getNamespaceResourcePolicy() != null;

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> {
                        progress = proxy.initiate("AWS-Redshift-Cluster::DescribeLogging", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToDescribeStatusLoggingRequest)
                            .makeServiceCall(this::describeLoggingStatus)
                            .done(enableLoggingResponse -> {
                                LoggingProperties loggingProperties = LoggingProperties.builder()
                                        .logDestinationType(enableLoggingResponse.logDestinationTypeAsString())
                                        .logExports(enableLoggingResponse.logExports())
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
                    progress = proxy.initiate("AWS-Redshift-Cluster::DescribeCluster", proxyClient, progress.getResourceModel(), callbackContext)
                            .translateToServiceRequest(Translator::translateToDescribeClusterRequest)
                            .makeServiceCall(this::describeCluster)
                            .done(describeResponse -> ProgressEvent.progress(Translator.translateFromReadResponse(describeResponse), callbackContext));
                    return  progress;
                })
                .then(progress -> proxy.initiate("AWS-Redshift-ResourcePolicy::Get", proxyClient, progress.getResourceModel(), callbackContext)
                        .translateToServiceRequest(Translator::translateToGetResourcePolicy)
                        .makeServiceCall(this::getNamespaceResourcePolicy)
                        .done((_request, _response, _client, _model, _context) -> {
                            _model.setNamespaceResourcePolicy(Translator.convertStringToJson(_response.resourcePolicy().policy(), logger));
                            _model.setLoggingProperties(callbackContext.getLoggingProperties());
                            return ProgressEvent.defaultSuccessHandler(_model);
                        }));
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
     * Gets resource policy for Cluster
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return getResponse resource response
     */
    private GetResourcePolicyResponse getNamespaceResourcePolicy(
            final GetResourcePolicyRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        GetResourcePolicyResponse getResponse = null;

        try {
            getResponse = proxyClient.injectCredentialsAndInvokeV2(
                    awsRequest, proxyClient.client()::getResourcePolicy);
        } catch (ResourceNotFoundException e){
            logger.log(String.format("NamespaceResourcePolicy not found for namespace %s", awsRequest.resourceArn()));
            return noOpNamespaceResourcePoliy(awsRequest);
        } catch (InvalidPolicyException | UnsupportedOperationException e) {
            logger.log(String.format("Status Code: %s , Error Message: %s ", e.statusCode(), e.awsErrorDetails().errorMessage()));
            /* ResourcePolicy is not enabled in all regions, we should handle unsupported operation exception */
            if(e.statusCode() == RESOURCE_POLICY_UNSUPPORTED_ERR_STATUS_CODE &&
                    e.awsErrorDetails().errorMessage().contains(RESOURCE_POLICY_UNSUPPORTED_ERROR)) {
                logger.log(e.getMessage());
                return noOpNamespaceResourcePoliy(awsRequest);
            } else {
                throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
            }
        } catch (RedshiftException e) {
            /* This error handling is required for backward compatibility. Without this exception handling, existing customers creating
            or updating their clusters will see an error with permission issues - "is not authorized to perform: redshift:GetResourcePolicy",
            as Read handler is trying to hit getResourcePolicy APIs to get namespaceResourcePolicy details.*/
            /* This commit catches all the RedshiftException if NamespaceResourcePolicy is not in the CFN template,
             which might catch more exceptions than intended, we're doing this to unblock existing customers who
             don't specify the resource policy at all.
             We'll see if this all-inclusive catch makes sense, then make changes if needed.
             */
            if(!containsResourcePolicy) {
                logger.log(String.format("RedshiftException:  %s", e.getMessage()));
                return noOpNamespaceResourcePoliy(awsRequest);
            } else {
                throw new CfnGeneralServiceException(e);
            }
          } catch (SdkClientException | AwsServiceException e ) {
              throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        logger.log(String.format("%s  resource policy has successfully been read.", ResourceModel.TYPE_NAME));
        return getResponse;
    }

    /**
     * No Op method for assigning empty resource policy for Cluster create response.
     * @param awsRequest the aws service request to describe a resource
     * @return GetResourcePolicyResponse
     */
    private GetResourcePolicyResponse noOpNamespaceResourcePoliy(final GetResourcePolicyRequest awsRequest) {
        ResourcePolicy resourcePolicy = ResourcePolicy.builder()
                .resourceArn(awsRequest.resourceArn())
                .policy("")
                .build();
        return GetResourcePolicyResponse.builder().resourcePolicy(resourcePolicy).build();
    }
}
