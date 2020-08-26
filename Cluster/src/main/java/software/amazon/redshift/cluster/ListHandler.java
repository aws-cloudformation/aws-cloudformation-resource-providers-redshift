package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd{ //BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

//        final List<ResourceModel> models = new ArrayList<>();
//
//        DescribeClustersResponse describeClustersResponse = null;
//        try {
//            describeClustersResponse =
//                    proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
//                            ClientBuilder.getClient()::describeClusters);
//
//        } catch (final ClusterNotFoundException | InvalidTagException e) {
//            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getClusterIdentifier());
//        } catch (SdkClientException | RedshiftException e) {
//            throw new CfnGeneralServiceException(request.toString(), e);
//        }
//
//        return ProgressEvent.<ResourceModel, CallbackContext>builder()
//                .resourceModels(Translator.translateFromListRequest(describeClustersResponse))
//                .nextToken(describeClustersResponse.marker())
//                .status(OperationStatus.SUCCESS)
//                .build();


        return proxy.initiate("AWS-Redshift-ClusterParameterGroup::List", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest((m) -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall((awsRequest, client) -> {
                    DescribeClustersResponse awsResponse;
                    try {
                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
                    } catch (final AwsServiceException e) { // ResourceNotFoundException
                        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                    }
                    logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .success();

    }

//    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
//            final DescribeClustersResponse awsResponse) {
//        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse));
//    }
}
