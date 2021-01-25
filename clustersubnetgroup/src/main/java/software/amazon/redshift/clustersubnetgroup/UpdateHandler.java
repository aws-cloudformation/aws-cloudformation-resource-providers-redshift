package software.amazon.redshift.clustersubnetgroup;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-ClusterSubnetGroup::Update", proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall((modifyRequest, proxyInvocation) -> {
                            //Base on contract_update_non_existent_resource contract test
                            if (StringUtils.isNullOrEmpty(model.getSubnetGroupName())){
                                return ProgressEvent.defaultFailureHandler(
                                        new Exception("SubnetGroupName in update handler cannot be null"), HandlerErrorCode.NotFound);
                            }
                            ModifyClusterSubnetGroupResponse response =  proxyInvocation.injectCredentialsAndInvokeV2(
                                    modifyRequest, proxyInvocation.client()::modifyClusterSubnetGroup);
                            logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                            return response;
                        })
                        .handleError((modifyDbSubnetGroupRequest, exception, client, resourceModel, cxt) -> {
                            System.out.println(exception.toString());
                            if (exception instanceof ClusterSubnetGroupNotFoundException)
                                return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
                            throw exception;
                        })
                        .progress())
                .then(progress -> handleTagging(request, proxyClient, proxy, progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleTagging(
            ResourceHandlerRequest<ResourceModel> request,
            final ProxyClient<RedshiftClient> proxyClient,
            final AmazonWebServicesClientProxy proxy,
            final ProgressEvent<ResourceModel, CallbackContext> progress) {
        try {
            final String arn = Translator.getArn(request);
            final List<Tag> prevTags = Translator.getTags(arn, proxy, proxyClient);
            final List<Tag> currTags = Translator.translateTagsMapToTagCollection(request.getDesiredResourceTags());
            final Set<Tag> prevTagSet = CollectionUtils.isEmpty(prevTags) ? new HashSet<>() : new HashSet<>(prevTags);
            final Set<Tag> currTagSet = CollectionUtils.isEmpty(currTags) ? new HashSet<>() : new HashSet<>(currTags);

            List<Tag> tagsToCreate = Sets.difference(currTagSet, prevTagSet).immutableCopy().asList();
            List<String> tagsKeyToDelete = Sets.difference(Translator.getTagsKeySet(prevTagSet), Translator.getTagsKeySet(currTagSet)).immutableCopy().asList();

            if(CollectionUtils.isNotEmpty(tagsToCreate)) {
                proxy.injectCredentialsAndInvokeV2(Translator.createTagsRequest(tagsToCreate, arn), proxyClient.client()::createTags);
            }

            if(CollectionUtils.isNotEmpty(tagsKeyToDelete)) {
                proxy.injectCredentialsAndInvokeV2(Translator.deleteTagsRequest(tagsKeyToDelete, arn), proxyClient.client()::deleteTags);
            }

        } catch (final InvalidTagException | TagLimitExceededException e) {
            throw new CfnGeneralServiceException("updateTagging", e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getSubnetGroupName());
        }
        return progress;
    }

}
