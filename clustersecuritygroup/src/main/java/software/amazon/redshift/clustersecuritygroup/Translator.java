package software.amazon.redshift.clustersecuritygroup;

import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup;
import software.amazon.awssdk.services.redshift.model.CreateClusterSecurityGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSecurityGroupRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsResponse;
import software.amazon.awssdk.services.redshift.model.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateClusterSecurityGroupRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateClusterSecurityGroupRequest.builder()
                .clusterSecurityGroupName(model.getClusterSecurityGroupName())
                .description(model.getDescription())
                .tags(translateTagsMapToTagCollection(tags))
                .build();
    }

    static List<software.amazon.awssdk.services.redshift.model.Tag> translateTagsMapToTagCollection(final Map<String, String> tags) {
        if (tags == null) return null;
        return tags.keySet().stream()
                .map(key -> Tag.builder().key(key).value(tags.get(key)).build())
                .collect(Collectors.toList());
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeClusterSecurityGroupsRequest translateToReadRequest(final ResourceModel model) {

        return DescribeClusterSecurityGroupsRequest.builder().clusterSecurityGroupName(model.getClusterSecurityGroupName()).build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeClusterSecurityGroupsResponse awsResponse, final String name) {
        List<ClusterSecurityGroup> groups = awsResponse.clusterSecurityGroups().stream().
                filter(p -> p.clusterSecurityGroupName().equals(name)).collect(Collectors.toList());

        final String securityGroupName = streamOfOrEmpty(groups)
                .map(software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup::clusterSecurityGroupName)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);

        final String description = streamOfOrEmpty(groups)
                .map(software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup::description)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);

        final List<Tag> tags = streamOfOrEmpty(groups)
                .map(software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup::tags)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);

        return ResourceModel.builder()
                .clusterSecurityGroupName(securityGroupName)
                .description(description)
                .tags(translateTagsFromSdk(tags))
                .build();
    }

    static List<software.amazon.redshift.clustersecuritygroup.Tag> translateTagsFromSdk(final List<Tag> tags) {
        return Optional.ofNullable(tags).orElse(Collections.emptyList())
                .stream()
                .map(tag -> software.amazon.redshift.clustersecuritygroup.Tag.builder()
                        .key(tag.key())
                        .value(tag.value()).build())
                .collect(Collectors.toList());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteClusterSecurityGroupRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteClusterSecurityGroupRequest.builder().clusterSecurityGroupName(model.getClusterSecurityGroupName()).build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeClusterSecurityGroupsRequest translateToListRequest(final String nextToken) {
        return DescribeClusterSecurityGroupsRequest.builder().marker(nextToken).build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
