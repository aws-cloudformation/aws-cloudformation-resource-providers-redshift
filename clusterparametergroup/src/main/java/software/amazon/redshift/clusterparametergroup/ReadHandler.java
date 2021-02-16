package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-Redshift-ClusterParameterGroup::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((awsRequest, client) -> {
                DescribeClusterParameterGroupsResponse awsResponse;
                try {
                    awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameterGroups);
                } catch (final ClusterParameterGroupNotFoundException e) { // ResourceNotFoundException
                    throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getParameterGroupName());
                } catch (final AwsServiceException e) {
                    throw new CfnInvalidRequestException(awsRequest.toString(), e);
                }
                logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                return awsResponse;
            })
            .done((awsResponse) -> constructResourceModelFromResponse(awsResponse, model.getParameterGroupName()));
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeClusterParameterGroupsResponse awsResponse, final String parameterGroupName) {
        ResourceModel model = Translator.translateFromReadResponse(awsResponse, parameterGroupName);
        return model.getParameterGroupName() != null ? ProgressEvent.defaultSuccessHandler(model) :
                ProgressEvent.defaultFailureHandler(new CfnNotFoundException(ResourceModel.TYPE_NAME, parameterGroupName), HandlerErrorCode.NotFound);
    }
}
