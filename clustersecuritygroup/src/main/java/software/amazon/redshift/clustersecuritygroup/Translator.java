package software.amazon.redshift.clustersecuritygroup;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup;
import software.amazon.awssdk.services.redshift.model.CreateClusterSecurityGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSecurityGroupRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsResponse;
import software.amazon.awssdk.services.redshift.model.Tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

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
    static ResourceModel translateFromReadResponse(final DescribeClusterSecurityGroupsResponse awsResponse) {
        if (!awsResponse.hasClusterSecurityGroups()) {
            return new ResourceModel();
        }
        ClusterSecurityGroup securityGroup = awsResponse.clusterSecurityGroups().get(0);
        return ResourceModel.builder()
                .clusterSecurityGroupName(securityGroup.clusterSecurityGroupName())
                .description(securityGroup.description())
                .build();
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
     * Request to update properties of a previously created resource
     *
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
     *
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
     *
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
     *
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
