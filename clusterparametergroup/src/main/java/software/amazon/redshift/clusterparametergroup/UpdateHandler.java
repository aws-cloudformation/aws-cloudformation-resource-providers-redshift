package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
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
                .then(progress -> {
                    if (CollectionUtils.isEmpty(model.getParameters())) {
                        return proxy.initiate("AWS-Redshift-ClusterParameterGroup::Reset", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToResetRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    ResetClusterParameterGroupResponse awsResponse;
                                    try {
                                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::resetClusterParameterGroup);
                                        logger.log(" UpdateHandler:: RESET:: awsRequest " + awsRequest);
                                        logger.log(" UpdateHandler:: RESET:: awsResponse " + awsResponse);
                                    } catch (final InvalidClusterParameterGroupStateException e) {
                                        throw new CfnInvalidRequestException(awsRequest.toString(), e);
                                    } catch (final ClusterParameterGroupNotFoundException e) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                                    }
                                    logger.log(String.format("%s has successfully been reset.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                }).progress();
                    } else {
                        return proxy.initiate("AWS-Redshift-ClusterParameterGroup::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    ModifyClusterParameterGroupResponse awsResponse;
                                    try {
                                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::modifyClusterParameterGroup);
                                        logger.log(" UpdateHandler:: UPDATE:: awsRequest " + awsRequest );
                                        logger.log(" UpdateHandler:: UPDATE:: awsResponse " + awsResponse);
                                    } catch (final InvalidClusterParameterGroupStateException e) {
                                        throw new CfnInvalidRequestException(awsRequest.toString(), e);
                                    } catch (final ClusterParameterGroupNotFoundException e) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                                    }
                                    logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                }).progress();
                    }
                })
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

            logger.log(" UpdateHandler: arn " + arn);
            final List<Tag> prevTags = Translator.getTags(arn, proxy, proxyClient); // read from redshift
            final List<Tag> currTags = Translator.translateTagsMapToTagCollection(request.getDesiredResourceTags());

            final Set<Tag> prevTagSet = CollectionUtils.isEmpty(prevTags) ? new HashSet<>() : new HashSet<>(prevTags);
            final Set<Tag> currTagSet = CollectionUtils.isEmpty(currTags) ? new HashSet<>() : new HashSet<>(currTags);

            List<Tag> tagsToCreate = Sets.difference(currTagSet, prevTagSet).immutableCopy().asList();
             List<String> tagsKeyToDelete = Sets.difference(Translator.getTagsKeySet(prevTagSet), Translator.getTagsKeySet(currTagSet)).immutableCopy().asList();
            logger.log(" UpdateHandler:: tagsToCreate " + tagsToCreate.toString());

            if (CollectionUtils.isNotEmpty(tagsToCreate)) {
                // if existing keys, the value for that key will be updated with the new value.
                proxy.injectCredentialsAndInvokeV2(Translator.createTagsRequest(tagsToCreate, arn), proxyClient.client()::createTags);
            }

            if (CollectionUtils.isNotEmpty(tagsKeyToDelete)) {
                proxy.injectCredentialsAndInvokeV2(Translator.deleteTagsRequest(tagsKeyToDelete, arn), proxyClient.client()::deleteTags);
            }
            logger.log(" UpdateHandler:: handleTagging completed, updated: " +tagsToCreate.toString());

        } catch (final InvalidTagException | TagLimitExceededException e) {
            throw new CfnGeneralServiceException("updateTagging failure", e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getParameterGroupName());
        }
        return progress;
    }

}


