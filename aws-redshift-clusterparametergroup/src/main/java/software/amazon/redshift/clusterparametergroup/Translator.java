package software.amazon.redshift.clusterparametergroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.redshift.model.CreateClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static software.amazon.redshift.clusterparametergroup.UpdateHandler.NEED_TO_BE_RESET;

public class Translator {
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateClusterParameterGroupRequest translateToCreateRequest(final ResourceModel model) {
        return CreateClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .parameterGroupFamily(model.getParameterGroupFamily())
                .description(model.getDescription())
                .tags(translateToSdkTags(model.getTags()))
                .build();
    }

    /**
     * Request to read Parameters for a ClusterParameterGroup
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe resource's Parameters
     */
    static DescribeClusterParametersRequest translateToReadParametersRequest(final ResourceModel model) {
        return DescribeClusterParametersRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource Parameters response
     * @return model resource model
     */
    static ResourceModel translateFromReadParametersResponse(final DescribeClusterParametersResponse awsResponse,
                                                             final ResourceModel model) {
        return model.toBuilder()
                .parameters(translateToModelParameters(awsResponse.parameters()
                        .stream()
                        .filter(parameter -> parameter.source().equalsIgnoreCase("user"))
                        .collect(Collectors.toList())))
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeClusterParameterGroupsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeClusterParameterGroupsRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeClusterParameterGroupsResponse awsResponse) {
        return awsResponse.parameterGroups()
                .stream()
                .map(clusterParameterGroup -> ResourceModel.builder()
                        .parameterGroupName(clusterParameterGroup.parameterGroupName())
                        .description(clusterParameterGroup.description())
                        .parameterGroupFamily(clusterParameterGroup.parameterGroupFamily())
                        .tags(translateToModelTags(clusterParameterGroup.tags()))
                        .build())
                .findAny()
                .orElse(ResourceModel.builder().build());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteClusterParameterGroupRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeClusterParameterGroupsRequest translateToListRequest(final String nextToken) {
        return DescribeClusterParameterGroupsRequest.builder()
                .marker(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final DescribeClusterParameterGroupsResponse awsResponse) {
        return awsResponse.parameterGroups()
                .stream()
                .map(clusterParameterGroup -> ResourceModel.builder()
                        .parameterGroupName(clusterParameterGroup.parameterGroupName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Request to read tags for a resource
     *
     * @param resourceName the arn of the requested resource
     * @return awsRequest the aws service request to update tags of a resource
     */
    static DescribeTagsRequest translateToReadTagsRequest(final String resourceName) {
        return DescribeTagsRequest.builder()
                .resourceName(resourceName)
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return awsRequest the aws service request to update tags of a resource
     */
    static ResourceModel translateFromReadTagsResponse(final DescribeTagsResponse awsResponse) {
        return ResourceModel.builder()
                .tags(translateToModelTags(awsResponse.taggedResources()
                        .stream()
                        .map(TaggedResource::tag)
                        .collect(Collectors.toList())))
                .build();
    }

    /**
     * Request to update tags for a resource
     *
     * @param desiredResourceState the resource model request to update tags
     * @param currentResourceState the resource model request to delete tags
     * @param resourceName         the arn of the requested resource
     * @return awsRequest the aws service request to update tags of a resource
     */
    static ModifyTagsRequest translateToUpdateTagsRequest(final ResourceModel desiredResourceState,
                                                          final ResourceModel currentResourceState,
                                                          final String resourceName) {
        List<Tag> toBeCreatedTags = subtract(desiredResourceState.getTags(), currentResourceState.getTags());
        List<Tag> toBeDeletedTags = subtract(currentResourceState.getTags(), desiredResourceState.getTags());

        return ModifyTagsRequest.builder()
                .createNewTagsRequest(CreateTagsRequest.builder()
                        .tags(translateToSdkTags(toBeCreatedTags))
                        .resourceName(resourceName)
                        .build())
                .deleteOldTagsRequest(DeleteTagsRequest.builder()
                        .tagKeys(toBeDeletedTags
                                .stream()
                                .map(Tag::getKey)
                                .collect(Collectors.toList()))
                        .resourceName(resourceName)
                        .build())
                .build();
    }

    /**
     * Request to reset properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    public static ResetClusterParameterGroupRequest translateToResetRequest(ResourceModel model) {
        return ResetClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .resetAllParameters(false)
                .parameters(model.getParameters()
                        .stream()
                        .filter(parameter -> StringUtils.equalsIgnoreCase(parameter.getParameterValue(), NEED_TO_BE_RESET))
                        .map(Translator::translateToSdkParameter)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static ModifyClusterParameterGroupRequest translateToUpdateRequest(final ResourceModel model) {
        return ModifyClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .parameters(model.getParameters()
                        .stream()
                        .filter(parameter -> !StringUtils.equalsIgnoreCase(parameter.getParameterValue(), NEED_TO_BE_RESET))
                        .map(Translator::translateToSdkParameter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static software.amazon.awssdk.services.redshift.model.Parameter translateToSdkParameter(Parameter parameter) {
        return GSON.fromJson(GSON.toJson(parameter), software.amazon.awssdk.services.redshift.model.Parameter.class);
    }

    public static List<software.amazon.awssdk.services.redshift.model.Parameter> translateToSdkParameters(List<Parameter> parameters) {
        return Optional.ofNullable(parameters)
                .map(ps -> ps
                        .stream()
                        .map(Translator::translateToSdkParameter)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private static Parameter translateToModelParameter(software.amazon.awssdk.services.redshift.model.Parameter parameter) {
        return GSON.fromJson(GSON.toJson(parameter), Parameter.class);
    }

    private static List<Parameter> translateToModelParameters(List<software.amazon.awssdk.services.redshift.model.Parameter> parameters) {
        return Optional.ofNullable(parameters)
                .map(ps -> ps
                        .stream()
                        .map(Translator::translateToModelParameter)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private static software.amazon.awssdk.services.redshift.model.Tag translateToSdkTag(Tag tag) {
        return GSON.fromJson(GSON.toJson(tag), software.amazon.awssdk.services.redshift.model.Tag.class);
    }

    public static List<software.amazon.awssdk.services.redshift.model.Tag> translateToSdkTags(List<Tag> tags) {
        return Optional.ofNullable(tags)
                .map(ts -> ts
                        .stream()
                        .map(Translator::translateToSdkTag)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private static Tag translateToModelTag(software.amazon.awssdk.services.redshift.model.Tag tag) {
        return GSON.fromJson(GSON.toJson(tag), Tag.class);
    }

    private static List<Tag> translateToModelTags(List<software.amazon.awssdk.services.redshift.model.Tag> tags) {
        return Optional.ofNullable(tags)
                .map(ts -> ts
                        .stream()
                        .map(Translator::translateToModelTag)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Do subtract for two lists: a - b
     *
     * @param a   the list that wants to minus b
     * @param b   the list that would be subtracted by a
     * @param <T> the class of a's and b's elements
     * @return (a - b) a.k.a (a minus b)
     */
    private static <T> List<T> subtract(List<T> a, List<T> b) {
        return Optional.ofNullable(a)
                .map(aIfNotNull -> aIfNotNull
                        .stream()
                        .filter(ao -> Optional.ofNullable(b)
                                .map(bIfNotNull -> !bIfNotNull.contains(ao))
                                .orElse(true))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
