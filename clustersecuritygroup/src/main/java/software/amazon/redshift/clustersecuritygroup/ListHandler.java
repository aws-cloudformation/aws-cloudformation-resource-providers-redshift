package software.amazon.redshift.clustersecuritygroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        final DescribeClusterSecurityGroupsResponse awsResponse = proxy.injectCredentialsAndInvokeV2(
                Translator.translateToListRequest(request.getNextToken()), proxyClient.client()::describeClusterSecurityGroups);

        logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(awsResponse
                        .clusterSecurityGroups()
                        .stream().map(dbClusterParameterGroup -> ResourceModel.builder().clusterSecurityGroupName(
                                dbClusterParameterGroup.clusterSecurityGroupName()).description(dbClusterParameterGroup.description()).build())
                        .collect(Collectors.toList()))
                .nextToken(awsResponse.marker())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
