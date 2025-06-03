package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandler<CallbackContext> {
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        this.logger = logger;

        DescribeClusterSubnetGroupsRequest awsRequest = Translator.translateToListRequest(request.getNextToken());
        DescribeClusterSubnetGroupsResponse awsResponse = listClusterSubnetGroups(awsRequest, proxy);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(Translator.translateFromListRequest(awsResponse))  // Updated method name
                .nextToken(awsResponse.marker())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private DescribeClusterSubnetGroupsResponse listClusterSubnetGroups(
            final DescribeClusterSubnetGroupsRequest awsRequest,
            final AmazonWebServicesClientProxy proxy) {

        DescribeClusterSubnetGroupsResponse awsResponse;

        try {
            awsResponse = proxy.injectCredentialsAndInvokeV2(
                    awsRequest,
                    ClientBuilder.getClient()::describeClusterSubnetGroups);

        } catch (final ClusterSubnetGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    awsRequest.clusterSubnetGroupName(), e);

        } catch (final InvalidTagException e) {
            throw new CfnInvalidRequestException(e);

        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}