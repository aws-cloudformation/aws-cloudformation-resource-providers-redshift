package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSecurityGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterTrackException;
import software.amazon.awssdk.services.redshift.model.InvalidElasticIpException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.LimitExceededException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterResponse;
import software.amazon.awssdk.services.redshift.model.RebootClusterRequest;
import software.amazon.awssdk.services.redshift.model.RebootClusterResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.awssdk.services.redshift.model.UnsupportedOptionException;
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

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        boolean clusterExists = isClusterAvailableForUpdate(proxyClient, model);
        if(!clusterExists) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(HandlerErrorCode.NotFound.getMessage())
                    .build();
        }


        if(!CollectionUtils.isNullOrEmpty(model.getAddIamRoles())
                || !CollectionUtils.isNullOrEmpty(model.getRemoveIamRoles())) {

            return ProgressEvent.progress(model, callbackContext)
                    .then(progress -> proxy.initiate("AWS-Redshift-Cluster::UpdateClusterIAMRoles", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToUpdateIAMRolesRequest)
                            .makeServiceCall(this::updateIAMRoles)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress())
                    .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        }
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-Cluster::UpdateCluster", proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::updateResource)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
        }

    private ModifyClusterResponse updateResource(
            final ModifyClusterRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyCluster);
        } catch (final InvalidClusterStateException | InvalidClusterSecurityGroupStateException | UnauthorizedOperationException
                | UnsupportedOptionException | LimitExceededException | InvalidElasticIpException | InvalidClusterTrackException | InvalidRetentionPeriodException
                | DependentServiceRequestThrottlingException | ClusterSubnetQuotaExceededException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterNotFoundException | ClusterSecurityGroupNotFoundException | ClusterParameterGroupNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyRequest.toString(), e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

    private ModifyClusterIamRolesResponse updateIAMRoles(
            final ModifyClusterIamRolesRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterIamRolesResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyClusterIamRoles);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, modifyRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(modifyRequest.toString(), e);
        }

        logger.log(String.format("%s IAM Roles successfully updated.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }


    private RebootClusterResponse rebootCluster(
            final RebootClusterRequest rebootClusterRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        RebootClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(rebootClusterRequest, proxyClient.client()::rebootCluster);
        } catch (final InvalidClusterStateException e ) {
            throw new CfnInvalidRequestException(rebootClusterRequest.toString(), e);
        } catch (final ClusterNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, rebootClusterRequest.clusterIdentifier());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(rebootClusterRequest.toString(), e);
        }

        logger.log(String.format("%s Cluster Reboot.", ResourceModel.TYPE_NAME));

        return awsResponse;
    }

//    protected boolean isClusterAvailableForUpdate (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model) {
//        DescribeClustersRequest awsRequest =
//                DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
//        try {
//            DescribeClustersResponse awsResponse =
//                    proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
//        } catch (final ClusterNotFoundException e) {
//            logger.log(String.format("%s successfully deleted.", model.getClusterIdentifier()));
//            return false;
//        }
//        return true;
//    }
}
