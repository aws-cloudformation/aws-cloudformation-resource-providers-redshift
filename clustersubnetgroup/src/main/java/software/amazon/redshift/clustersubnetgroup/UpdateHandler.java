package software.amazon.redshift.clustersubnetgroup;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
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
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private RedshiftClient client;
    private AmazonWebServicesClientProxy proxy;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext callbackContext;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        this.client = ClientBuilder.getClient();
        this.proxy = proxy;
        this.request = request;
        this.callbackContext = callbackContext;
        this.logger = logger;
        ModifyClusterSubnetGroupRequest modifyRequest= Translator.modifyClusterSubnetGroupRequest(model);

        try {
            proxy.injectCredentialsAndInvokeV2(modifyRequest, client::modifyClusterSubnetGroup);
        } catch (final InvalidSubnetException | SubnetAlreadyInUseException | UnauthorizedOperationException
                | DependentServiceRequestThrottlingException | ClusterSubnetQuotaExceededException e ) {
            throw new CfnInvalidRequestException(modifyRequest.toString(), e);
        } catch (final ClusterSubnetGroupNotFoundException e){
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getSubnetGroupName());
        }

        handleTagging(request, proxy, client);
        logger.log(String.format("%s [%s] Updated Successfully", ResourceModel.TYPE_NAME, model.getSubnetGroupName()));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private void handleTagging(ResourceHandlerRequest<ResourceModel> request, final AmazonWebServicesClientProxy proxy, final RedshiftClient client) {
        try {
            final String arn = getArn(request);
            final List<Tag> prevTags = getTags(arn, proxy, client);
            final List<Tag> currTags = Translator.translateTagsToSdk(request.getDesiredResourceTags(), request.getDesiredResourceState().getTags());
            final Set<Tag> prevTagSet = CollectionUtils.isEmpty(prevTags) ? new HashSet<>() : new HashSet<>(prevTags);
            final Set<Tag> currTagSet = CollectionUtils.isEmpty(currTags) ? new HashSet<>() : new HashSet<>(currTags);

            List<Tag> tagsToCreate = Sets.difference(currTagSet, prevTagSet).immutableCopy().asList();
            List<String> tagsKeyToDelete = Sets.difference(Translator.getTagsKeySet(prevTagSet), Translator.getTagsKeySet(currTagSet)).immutableCopy().asList();

            if(CollectionUtils.isNotEmpty(tagsToCreate)) {
                proxy.injectCredentialsAndInvokeV2(Translator.createTagsRequest(tagsToCreate, arn), client::createTags);
            }

            if(CollectionUtils.isNotEmpty(tagsKeyToDelete)) {
                proxy.injectCredentialsAndInvokeV2(Translator.deleteTagsRequest(tagsKeyToDelete, arn), client::deleteTags);
            }

        } catch (final InvalidTagException | TagLimitExceededException e) {
            throw new CfnGeneralServiceException("updateTagging", e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getSubnetGroupName());
        }
    }

    private String getArn(final ResourceHandlerRequest<ResourceModel> request) {
        final String subnetGroupName = request.getDesiredResourceState().getSubnetGroupName();
        // TODO: use request.getAwsPartition() once implemented
        String partition = "aws";
        if (request.getRegion().indexOf("us-gov-") == 0) partition = partition.concat("-us-gov");
        if (request.getRegion().indexOf("cn-") == 0) partition = partition.concat("-cn");
        return String.format("arn:%s:redshift:%s:%s:subnetgroup:%s", partition, request.getRegion(), request.getAwsAccountId(), subnetGroupName);
    }

    private List<software.amazon.awssdk.services.redshift.model.Tag> getTags(final String arn, final AmazonWebServicesClientProxy proxy, final RedshiftClient client) {
        final DescribeTagsResponse response = proxy.injectCredentialsAndInvokeV2(Translator.describeTagsRequest(arn), client::describeTags);
        return response.taggedResources().stream().map(TaggedResource::tag).collect(Collectors.toList());
    }
}
