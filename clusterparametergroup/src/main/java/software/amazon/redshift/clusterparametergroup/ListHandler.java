package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.proxy.*;

import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        final DescribeClusterParameterGroupsResponse describeDbClusterParameterGroupsResponse = proxy.injectCredentialsAndInvokeV2(
                Translator.translateToListRequest(request.getNextToken()), proxyClient.client()::describeClusterParameterGroups);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(describeDbClusterParameterGroupsResponse
                        .parameterGroups()
                        .stream().map(dbClusterParameterGroup -> ResourceModel.builder().parameterGroupName(
                                dbClusterParameterGroup.parameterGroupName()).parameterGroupFamily(dbClusterParameterGroup.parameterGroupFamily()).build())
                        .collect(Collectors.toList()))
                .nextToken(describeDbClusterParameterGroupsResponse.marker())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
