package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
        client = ClientBuilder.getClient();
        this.proxy = proxy;
        this.request = request;
        this.callbackContext = callbackContext;
        this.logger = logger;

        try {
            if (CollectionUtils.isEmpty(model.getParameters())) {
                proxy.injectCredentialsAndInvokeV2(Translator.resetClusterParameterGroupRequest(model), client::resetClusterParameterGroup);
            } else {
                proxy.injectCredentialsAndInvokeV2(Translator.modifyClusterParameterGroupRequest(model), client::modifyClusterParameterGroup);
            }
        } catch (InvalidClusterParameterGroupStateException e) {
            throw new CfnGeneralServiceException(e);
        } catch (ClusterParameterGroupNotFoundException e) {
            throw new CfnNotFoundException(e);
        }

        handleTagging(request.getDesiredResourceTags(), ReadHandler.getArn(model.getParameterGroupName(), request), proxy, client);
        logger.log(String.format("%s [%s] Updated Successfully", ResourceModel.TYPE_NAME, model.getParameterGroupName()));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void handleTagging(final Map<String, String> tags, final String arn, final AmazonWebServicesClientProxy proxy, final RedshiftClient client) {
        final List<Tag> prevTags = Translator.translateTagsFromSdk(ReadHandler.getTags(arn, proxy, client));
        final Set<Tag> prevTagSet = CollectionUtils.isEmpty(prevTags) ? new HashSet<>() : new HashSet<>(prevTags);
        final Set<Tag> currTagSet = MapUtils.isEmpty(tags) ? new HashSet<>() : tags.keySet().stream().map(key -> new Tag(key, tags.get(key))).collect(Collectors.toSet());

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.createTagsRequest(Translator.translateTagsToSdk(Sets.difference(currTagSet, prevTagSet).immutableCopy().asList()), arn), client::createTags);
            proxy.injectCredentialsAndInvokeV2(Translator.deleteTagsRequest(Translator.translateTagsToSdk(Sets.difference(prevTagSet, currTagSet).immutableCopy().asList()), arn), client::deleteTags);
        } catch (InvalidTagException e) {
            throw new CfnGeneralServiceException(e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(e);
        }
    }
}