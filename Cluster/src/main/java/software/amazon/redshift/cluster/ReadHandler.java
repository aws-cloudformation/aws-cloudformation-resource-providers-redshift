package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

        System.out.println("In READ HAndler cluster id = "+request.getDesiredResourceState().getClusterIdentifier());

        return proxy.initiate("AWS-Redshift-Cluster::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::readResource)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeClustersResponse readResource(
            final DescribeClustersRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClustersResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
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
}