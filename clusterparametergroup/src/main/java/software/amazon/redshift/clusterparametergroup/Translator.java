package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
  private static final int MAX_RECORDS_TO_DESCRIBE = 20;

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
  static ModifyClusterParameterGroupRequest translateToUpdateRequest(final ResourceModel model) {
    return ModifyClusterParameterGroupRequest.builder()
            .parameterGroupName(model.getParameterGroupName())
            .parameters(translateParametersToSdk(model.getParameters()))
            .build();
  }

  private static List<software.amazon.awssdk.services.redshift.model.Parameter> translateParametersToSdk(final List<software.amazon.redshift.clusterparametergroup.Parameter> parameters) {
    return parameters.stream()
            .map(param -> software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName(param.getParameterName()).parameterValue(param.getParameterValue()).build())
            .collect(Collectors.toList());
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
  static DescribeClusterParameterGroupsRequest translateToListRequest(final String nextToken) {
    return DescribeClusterParameterGroupsRequest.builder().marker(nextToken).build();
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

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListResponse(final DescribeClusterParameterGroupsResponse awsResponse) {
    return streamOfOrEmpty(awsResponse.parameterGroups())
            .map(clusterParameterGroup -> ResourceModel.builder()
                    .parameterGroupName(clusterParameterGroup.parameterGroupName())
                    .build())
            .collect(Collectors.toList());
  }
  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }

  static String getArn(final ResourceHandlerRequest<ResourceModel> request) {
    String partition = request.getAwsPartition();
    final String parameterGroupName = request.getDesiredResourceState().getParameterGroupName();
//        String partition = "aws";
//        if (request.getRegion().indexOf("us-gov-") == 0) partition = partition.concat("-us-gov");
//        if (request.getRegion().indexOf("cn-") == 0) partition = partition.concat("-cn");
    return String.format("arn:%s:redshift:%s:%s:parametergroup:%s", partition, request.getRegion(), request.getAwsAccountId(), parameterGroupName);
  }

  static List<Tag> getTags(final String arn, final AmazonWebServicesClientProxy proxy, final ProxyClient<RedshiftClient> proxyClient) {
    final DescribeTagsResponse response = proxy.injectCredentialsAndInvokeV2(Translator.describeTagsRequest(arn), proxyClient.client()::describeTags);
    return response.taggedResources().stream().map(TaggedResource::tag).collect(Collectors.toList());
  }

  static DescribeTagsRequest describeTagsRequest(final String arn) {
    return DescribeTagsRequest.builder()
            .resourceName(arn)
            .build();
  }

  static Set<String> getTagsKeySet(final Collection<Tag> tags) {
    return tags.stream().map(tag -> tag.key()).collect(Collectors.toSet());
  }

  static CreateTagsRequest createTagsRequest(final Collection<Tag> tags, final String arn) {
    return CreateTagsRequest.builder()
            .resourceName(arn)
            .tags(tags)
            .build();
  }

  static DeleteTagsRequest deleteTagsRequest(final Collection<String> tagsKey, final String arn) {
    return DeleteTagsRequest.builder()
            .resourceName(arn)
            .tagKeys(tagsKey)
            .build();
  }

  public static DescribeClusterParametersRequest describeClusterParametersRequest(final ResourceModel model, final String marker) {
    return DescribeClusterParametersRequest.builder()
            .parameterGroupName(model.getParameterGroupName())
            .marker(marker)
            .maxRecords(MAX_RECORDS_TO_DESCRIBE)
            .build();
  }

  public static Set<software.amazon.awssdk.services.redshift.model.Parameter> getParametersToModify(
          ResourceModel model, List<software.amazon.awssdk.services.redshift.model.Parameter> parameters) {
    // find all the parameter already in model
    Set<String> existingParameterKeys = model.getParameters().stream()
            .map(Parameter::getParameterName).collect(Collectors.toSet());
    return parameters.stream().filter(parameter -> existingParameterKeys.contains(parameter.parameterName()))
            .collect(Collectors.toSet());
  }

  public static ModifyClusterParameterGroupRequest modifyDbClusterParameterGroupRequest(ResourceModel resourceModel,
                                                            Set<software.amazon.awssdk.services.redshift.model.Parameter> params) {
    return ModifyClusterParameterGroupRequest.builder()
            .parameterGroupName(resourceModel.getParameterGroupName())
            .parameters(params)
            .build();
  }
}
