package software.amazon.redshift.clustersecuritygroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
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

        return proxy.initiate("AWS-Redshift-ClusterSecurityGroup::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, client) -> {
                    DescribeClusterSecurityGroupsResponse awsResponse = null;
                    try {
                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterSecurityGroups);
                    } catch (final ClusterSecurityGroupNotFoundException e) {
                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getClusterSecurityGroupName());
                    } catch (final AwsServiceException e) {
                        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
                    }
                    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .done((awsResponse) -> constructResourceModelFromResponse(awsResponse, request.getDesiredResourceState().getClusterSecurityGroupName()));
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     *
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeClusterSecurityGroupsResponse awsResponse, final String securityGroupName) {
        ResourceModel model = Translator.translateFromReadResponse(awsResponse, securityGroupName);
        return model.getClusterSecurityGroupName() != null ? ProgressEvent.defaultSuccessHandler(model) :
                ProgressEvent.defaultFailureHandler(new CfnNotFoundException(ResourceModel.TYPE_NAME, securityGroupName), HandlerErrorCode.NotFound);
    }
}
