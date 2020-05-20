package software.amazon.redshift.clustersubnetgroup;

import com.amazonaws.util.CollectionUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
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
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
  static CreateClusterSubnetGroupRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
    return CreateClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getSubnetGroupName())
            .subnetIds(model.getSubnetIds())
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
  static DescribeClusterSubnetGroupsRequest translateToReadRequest(final ResourceModel model) {
    return DescribeClusterSubnetGroupsRequest.builder()
            .clusterSubnetGroupName(model.getSubnetGroupName())
            .build();
  }

  static DescribeClusterSubnetGroupsRequest translateToListRequest(final String nextToken) {
    return DescribeClusterSubnetGroupsRequest.builder().marker(nextToken).build();
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
            .subnetGroupName(subnetGroupName)
            .description(description)
            .subnetIds(translateSubnetIdsFromSdk(subnetIds))
            .tags(translateTagsFromSdk(tags))
            .build();
  }

  static List<String> translateSubnetIdsFromSdk (final List<Subnet> subnets) {
    return subnets.stream().map(subnet -> subnet.subnetIdentifier()).collect(Collectors.toList());

  }

  static List<software.amazon.redshift.clustersubnetgroup.Tag> translateTagsFromSdk (final List<Tag> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
            .map(tag -> software.amazon.redshift.clustersubnetgroup.Tag.builder()
                    .key(tag.key())
                    .value(tag.value()).build())
            .collect(Collectors.toList());
  }


  static DeleteClusterSubnetGroupRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getSubnetGroupName())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterSubnetGroupRequest translateToUpdateRequest(final ResourceModel model) {
    return ModifyClusterSubnetGroupRequest.builder()
            .clusterSubnetGroupName(model.getSubnetGroupName())
            .subnetIds(model.getSubnetIds())
            .description(model.getDescription())
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListResponse(final DescribeClusterSubnetGroupsResponse awsResponse) {
    return streamOfOrEmpty(awsResponse.clusterSubnetGroups())
        .map(clusterSubnetGroup -> ResourceModel.builder()
                .subnetGroupName(clusterSubnetGroup.clusterSubnetGroupName())
                .build())
        .collect(Collectors.toList());
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

  static String getArn(final ResourceHandlerRequest<ResourceModel> request) {
    final String subnetGroupName = request.getDesiredResourceState().getSubnetGroupName();
    String partition = "aws";
    if (request.getRegion().indexOf("us-gov-") == 0) partition = partition.concat("-us-gov");
    if (request.getRegion().indexOf("cn-") == 0) partition = partition.concat("-cn");
    return String.format("arn:%s:redshift:%s:%s:subnetgroup:%s", partition, request.getRegion(), request.getAwsAccountId(), subnetGroupName);
  }

  static List<Tag> getTags(final String arn, final AmazonWebServicesClientProxy proxy, final ProxyClient<RedshiftClient> proxyClient) {
    final DescribeTagsResponse response = proxy.injectCredentialsAndInvokeV2(Translator.describeTagsRequest(arn), proxyClient.client()::describeTags);
    return response.taggedResources().stream().map(TaggedResource::tag).collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
