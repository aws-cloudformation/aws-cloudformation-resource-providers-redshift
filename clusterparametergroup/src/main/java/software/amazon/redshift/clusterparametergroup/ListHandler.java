package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        return proxy.initiate("AWS-Redshift-ClusterParameterGroup::List", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest((m) -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall((awsRequest, client) -> {
                    DescribeClusterParameterGroupsResponse awsResponse;
                    try {
                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameterGroups);
                    } catch (final AwsServiceException e) { // ResourceNotFoundException
                        throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e); // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/commit/2077c92299aeb9a68ae8f4418b5e932b12a8b186#diff-5761e3a9f732dc1ef84103dc4bc93399R56-R63
                    }
                    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .success();
    }
}
