package software.amazon.redshift.endpointauthorization;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    static AuthorizeEndpointAccessRequest translateToCreateRequest(final ResourceModel model) {
        return AuthorizeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .vpcIds(new ArrayList<>(model.getVpcIds()))
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeEndpointAuthorizationRequest translateToReadRequest(final ResourceModel model) {
        return DescribeEndpointAuthorizationRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .grantee(model.getAsGrantee())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final AwsResponse awsResponse) {
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L58-L73
        return ResourceModel.builder()
                //.someProperty(response.property())
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static RevokeEndpointAccessRequest translateToDeleteRequest(final ResourceModel model) {
        return RevokeEndpointAccessRequest.builder().build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static AuthorizeEndpointAccessRequest translateToUpdateRequest(final ResourceModel model) {
        // And update to add stuff is treated as a create request
        return translateToCreateRequest(model);
    }

    /**
     * Request to update some other properties that could not be provisioned through first update request
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static RevokeEndpointAccessRequest translateToRevokeRequest(final ResourceModel model) {
        return RevokeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .vpcIds(new ArrayList<>(model.getVpcIds()))
                .force(model.getForce())
                .build();
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
