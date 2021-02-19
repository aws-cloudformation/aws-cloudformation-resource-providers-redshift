package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsResponse;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.HsmClientCertificateNotFoundException;
import software.amazon.awssdk.services.redshift.model.HsmConfigurationNotFoundException;
import software.amazon.awssdk.services.redshift.model.InsufficientClusterCapacityException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSecurityGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterTrackException;
import software.amazon.awssdk.services.redshift.model.InvalidElasticIpException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.LimitExceededException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterResponse;
import software.amazon.awssdk.services.redshift.model.NumberOfNodesPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.NumberOfNodesQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.TableLimitExceededException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
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

import java.util.List;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;
    private String RESOURCE_NAME_PREFIX = "arn:aws:redshift:";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        boolean clusterExists = isClusterAvailableForUpdate(proxyClient, model, model.getClusterIdentifier());
        if(!clusterExists) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .message(HandlerErrorCode.NotFound.getMessage())
                    .build();
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> {
                    ProgressEvent<ResourceModel, CallbackContext> describeTags =
                            new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger);

                    String availabilityZone = describeTags.getResourceModel().getAvailabilityZone();
                    String region = availabilityZone.substring(0, availabilityZone.length() - 1);

                    List<Tag> existingTags = describeTags.getResourceModel().getTags();
                    List<List<Tag>> updateTags = updateTags(existingTags, model.getTags());

                    String resourceName = RESOURCE_NAME_PREFIX + region + ":" +model.getOwnerAccount() +
                            ":cluster:" + model.getClusterIdentifier();

                    if (!CollectionUtils.isNullOrEmpty(updateTags.get(CREATE_TAGS_INDEX))) {
                        return proxy.initiate("AWS-Redshift-Cluster::CreateTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest((createTagsRequest) -> Translator.translateToCreateTagsRequest(model, updateTags.get(CREATE_TAGS_INDEX), resourceName))
                                .makeServiceCall(this::createTags)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }
                    if (!CollectionUtils.isNullOrEmpty(updateTags.get(DELETE_TAGS_INDEX))) {
                        return proxy.initiate("AWS-Redshift-Cluster::DeleteTags", proxyClient, model, callbackContext)
                                .translateToServiceRequest((deleteTagsRequest) -> Translator.translateToDeleteTagsRequest(model, updateTags.get(DELETE_TAGS_INDEX), resourceName))
                                .makeServiceCall(this::deleteTags)
                                .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                                .progress();
                    }

                    return progress;
                })


                .then(progress -> {
                    List<List<String>> iamRolesForUpdate = iamRoleUpdate(request, model);
                    if (!CollectionUtils.isNullOrEmpty(iamRolesForUpdate)) {
                        return proxy.initiate("AWS-Redshift-Cluster::UpdateClusterIAMRoles", proxyClient, model, callbackContext)
                            .translateToServiceRequest((iamRolesModifyRequest) -> Translator.translateToUpdateIAMRolesRequest(model, iamRolesForUpdate))
                            .makeServiceCall(this::updateIAMRoles)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                    }
                    return progress;
                })

            .then(progress -> {
                if(issueModifyClusterRequest(model)) {
                    return proxy.initiate("AWS-Redshift-Cluster::UpdateCluster", proxyClient, model, callbackContext)
                            .translateToServiceRequest(Translator::translateToUpdateRequest)
                            .makeServiceCall(this::updateResource)
                            .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                            .progress();
                }
                return progress;
            })
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyClusterResponse updateResource(
            final ModifyClusterRequest modifyRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(modifyRequest, proxyClient.client()::modifyCluster);
        } catch (final InvalidClusterStateException | InvalidClusterSecurityGroupStateException | UnauthorizedOperationException |
                UnsupportedOptionException | LimitExceededException | InvalidElasticIpException | InvalidClusterTrackException |
                DependentServiceRequestThrottlingException | ClusterSubnetQuotaExceededException | NumberOfNodesQuotaExceededException |
                NumberOfNodesPerClusterLimitExceededException | InsufficientClusterCapacityException | HsmClientCertificateNotFoundException | HsmConfigurationNotFoundException |
                ClusterAlreadyExistsException | TableLimitExceededException | InvalidRetentionPeriodException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterNotFoundException | ClusterSecurityGroupNotFoundException |
                ClusterParameterGroupNotFoundException e) {
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

    private CreateTagsResponse createTags(
            final CreateTagsRequest createTagsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateTagsResponse createTagsResponse = null;

        try {
            createTagsResponse = proxyClient.injectCredentialsAndInvokeV2(createTagsRequest, proxyClient.client()::createTags);
        } catch (final InvalidClusterStateException | TagLimitExceededException | InvalidTagException e ) {
            throw new CfnInvalidRequestException(createTagsRequest.toString(), e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, createTagsRequest.resourceName());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(createTagsRequest.toString(), e);
        }

        logger.log(String.format("%s create tags for resource %s.", ResourceModel.TYPE_NAME,
                createTagsRequest.resourceName()));

        return createTagsResponse;
    }

    private DeleteTagsResponse deleteTags(
            final DeleteTagsRequest deleteTagsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteTagsResponse deleteTagsResponse = null;

        try {
            deleteTagsResponse = proxyClient.injectCredentialsAndInvokeV2(deleteTagsRequest, proxyClient.client()::deleteTags);
        } catch (final InvalidClusterStateException | TagLimitExceededException | InvalidTagException e ) {
            throw new CfnInvalidRequestException(deleteTagsRequest.toString(), e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteTagsRequest.resourceName());
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(deleteTagsRequest.toString(), e);
        }

        logger.log(String.format("%s delete tags for resource %s.", ResourceModel.TYPE_NAME,
                deleteTagsRequest.resourceName()));

        return deleteTagsResponse;
    }
}
