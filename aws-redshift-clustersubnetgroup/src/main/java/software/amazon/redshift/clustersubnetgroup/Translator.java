package software.amazon.redshift.clustersubnetgroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {
  private static final Gson GSON = new GsonBuilder().create();

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateClusterSubnetGroupRequest translateToCreateRequest(final String generateSubnetGroupName,
                                                                  final ResourceModel model,
                                                                  final Map<String, String> tags) {
    model.setClusterSubnetGroupName(generateSubnetGroupName);
    return CreateClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .subnetIds(model.getSubnetIds())
            .description(model.getDescription())
            .tags(translateToSdkTags(translateTagsMapToTagCollection(tags)))
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClusterSubnetGroupsRequest translateToReadRequest(final ResourceModel model) {
    if (StringUtils.isBlank(model.getClusterSubnetGroupName())) {
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
    }
    return DescribeClusterSubnetGroupsRequest.builder()
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeClusterSubnetGroupsResponse awsResponse) {
    final String subnetGroupName = streamOfOrEmpty(awsResponse.clusterSubnetGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup::clusterSubnetGroupName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    final String description = streamOfOrEmpty(awsResponse.clusterSubnetGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup::description)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<Subnet> subnetIds = streamOfOrEmpty(awsResponse.clusterSubnetGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup::subnets)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<Tag> tags = streamOfOrEmpty(awsResponse.clusterSubnetGroups())
            .map(software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup::tags)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    return ResourceModel.builder()
            .clusterSubnetGroupName(subnetGroupName)
            .description(description)
            .subnetIds(translateSubnetIdsFromSdk(subnetIds))
            .tags(translateTagsFromSdk(tags))
            .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterSubnetGroupRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterSubnetGroupRequest translateToUpdateRequest(final ResourceModel model) {
    return ModifyClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .subnetIds(model.getSubnetIds())
            .description(model.getDescription())
            .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static DescribeClusterSubnetGroupsRequest translateToListRequest(final String nextToken) {
    return DescribeClusterSubnetGroupsRequest.builder()
            .marker(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final DescribeClusterSubnetGroupsResponse awsResponse) {
    return streamOfOrEmpty(awsResponse.clusterSubnetGroups())
            .map(clusterSubnetGroup -> ResourceModel.builder()
                    .clusterSubnetGroupName(clusterSubnetGroup.clusterSubnetGroupName())
                    .description(clusterSubnetGroup.description())  // You might want to include these additional fields
                    .subnetIds(translateSubnetIdsFromSdk(clusterSubnetGroup.subnets()))
                    .tags(translateTagsFromSdk(clusterSubnetGroup.tags()))
                    .build())
            .collect(Collectors.toList());
  }

  // Tag-related methods
  static DescribeTagsRequest translateToReadTagsRequest(final String resourceName) {
    return DescribeTagsRequest.builder()
            .resourceName(resourceName)
            .build();
  }

  static ResourceModel translateFromReadTagsResponse(final ResourceModel model,
                                                     final DescribeTagsResponse awsResponse) {
    model.setTags(translateToModelTags(awsResponse.taggedResources()
            .stream()
            .map(TaggedResource::tag)
            .collect(Collectors.toList())));
    return model;
  }

  static ModifyTagsRequest translateToUpdateTagsRequest(
          List<software.amazon.redshift.clustersubnetgroup.Tag> desiredTags,
          List<software.amazon.redshift.clustersubnetgroup.Tag> currentTags,
          final String resourceName) {
    List<software.amazon.redshift.clustersubnetgroup.Tag> toBeCreatedTags = subtract(desiredTags, currentTags);
    List<software.amazon.redshift.clustersubnetgroup.Tag> toBeDeletedTags = subtract(currentTags, desiredTags);

    return ModifyTagsRequest.builder()
            .createNewTagsRequest(CreateTagsRequest.builder()
                    .tags(translateToSdkTags(toBeCreatedTags))
                    .resourceName(resourceName)
                    .build())
            .deleteOldTagsRequest(DeleteTagsRequest.builder()
                    .tagKeys(toBeDeletedTags
                            .stream()
                            .map(software.amazon.redshift.clustersubnetgroup.Tag::getKey)
                            .collect(Collectors.toList()))
                    .resourceName(resourceName)
                    .build())
            .build();
  }

  // Helper methods
  private static List<String> translateSubnetIdsFromSdk(final List<Subnet> subnets) {
    return subnets.stream()
            .map(Subnet::subnetIdentifier)
            .collect(Collectors.toList());
  }

  private static List<software.amazon.redshift.clustersubnetgroup.Tag> translateTagsFromSdk(final List<Tag> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
            .map(tag -> software.amazon.redshift.clustersubnetgroup.Tag.builder()
                    .key(tag.key())
                    .value(tag.value())
                    .build())
            .collect(Collectors.toList());
  }

  static List<software.amazon.redshift.clustersubnetgroup.Tag> translateTagsMapToTagCollection(final Map<String, String> tags) {
    if (tags == null) return null;
    return tags.keySet().stream()
            .map(key -> software.amazon.redshift.clustersubnetgroup.Tag.builder().key(key).value(tags.get(key)).build())
            .collect(Collectors.toList());
  }

  private static software.amazon.awssdk.services.redshift.model.Tag translateToSdkTag(Tag tag) {
    return GSON.fromJson(GSON.toJson(tag), software.amazon.awssdk.services.redshift.model.Tag.class);
  }

  public static List<software.amazon.awssdk.services.redshift.model.Tag> translateToSdkTags(
          List<software.amazon.redshift.clustersubnetgroup.Tag> tags) {
    return Optional.ofNullable(tags)
            .map(ts -> ts
                    .stream()
                    .map(tag -> software.amazon.awssdk.services.redshift.model.Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .collect(Collectors.toList()))
            .orElse(null);
  }

  private static software.amazon.redshift.clustersubnetgroup.Tag translateToModelTag(
          software.amazon.awssdk.services.redshift.model.Tag tag) {
    return GSON.fromJson(GSON.toJson(tag), software.amazon.redshift.clustersubnetgroup.Tag.class);
  }

  private static List<software.amazon.redshift.clustersubnetgroup.Tag> translateToModelTags(
          List<software.amazon.awssdk.services.redshift.model.Tag> tags) {
    return Optional.ofNullable(tags)
            .map(ts -> ts
                    .stream()
                    .map(Translator::translateToModelTag)
                    .collect(Collectors.toList()))
            .orElse(null);
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
  }

  static Map<String, String> translateFromResourceModelToSdkTags(final List<software.amazon.redshift.clustersubnetgroup.Tag> listOfTags) {
    Map<String, String> sdkTags = streamOfOrEmpty(listOfTags)
            .collect(Collectors.toMap(software.amazon.redshift.clustersubnetgroup.Tag::getKey, software.amazon.redshift.clustersubnetgroup.Tag::getValue));
    return sdkTags.isEmpty() ? null : sdkTags;
  }

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
