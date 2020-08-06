package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Tag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateClusterParameterGroupRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
    return CreateClusterParameterGroupRequest.builder()
            .parameterGroupName(model.getParameterGroupName())
            .parameterGroupFamily(model.getParameterGroupFamily())
            .description(model.getDescription())
            .tags(translateTagsMapToTagCollection(tags))
            .build();
  }

  static List<Tag> translateTagsMapToTagCollection(final Map<String, String> tags) {
    if (tags == null) return null;
    return tags.keySet().stream()
            .map(key -> Tag.builder().key(key).value(tags.get(key)).build())
            .collect(Collectors.toList());
  }
  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClusterParameterGroupsRequest translateToReadRequest(final ResourceModel model) {
    // 其他的参数要不要？？？
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L20-L24
    return DescribeClusterParameterGroupsRequest.builder()
            .parameterGroupName(model.getParameterGroupName())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeClusterParameterGroupsResponse awsResponse) {
    final String parameterGroupName = streamOfOrEmpty(awsResponse.parameterGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterParameterGroup::parameterGroupName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String description = streamOfOrEmpty(awsResponse.parameterGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterParameterGroup::description)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String parameterGroupFamily = streamOfOrEmpty(awsResponse.parameterGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterParameterGroup::parameterGroupFamily)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<Tag> tags = streamOfOrEmpty(awsResponse.parameterGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterParameterGroup::tags)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    return ResourceModel.builder()
            .parameterGroupName(parameterGroupName)
            .description(description)
            .parameterGroupFamily(parameterGroupFamily)
            .tags(translateTagsFromSdk(tags))
            .build();
  }

  static List<software.amazon.redshift.clusterparametergroup.Tag> translateTagsFromSdk (final List<Tag> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
            .map(tag -> software.amazon.redshift.clusterparametergroup.Tag.builder()
                    .key(tag.key())
                    .value(tag.value()).build())
            .collect(Collectors.toList());
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterParameterGroupRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteClusterParameterGroupRequest.builder()
            .parameterGroupName(model.getParameterGroupName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static AwsRequest translateToFirstUpdateRequest(final ResourceModel model) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L45-L50
    return awsRequest;
  }

  /**
   * Request to update some other properties that could not be provisioned through first update request
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static AwsRequest translateToSecondUpdateRequest(final ResourceModel model) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    return awsRequest;
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static AwsRequest translateToListRequest(final String nextToken) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L26-L31
    return awsRequest;
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final AwsResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
    return streamOfOrEmpty(Lists.newArrayList())
        .map(resource -> ResourceModel.builder()
            // include only primary identifier
            .build())
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
