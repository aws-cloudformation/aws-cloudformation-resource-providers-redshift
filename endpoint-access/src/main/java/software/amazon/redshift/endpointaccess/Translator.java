package software.amazon.redshift.endpointaccess;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;

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
     * @return the CreateEndpointAccessRequest to send to the Redshift API
     */
    static CreateEndpointAccessRequest translateToCreateRequest(final ResourceModel model) {
        return CreateEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .resourceOwner(model.getResourceOwner())
                .endpointName(model.getEndpointName())
                .subnetGroupName(model.getSubnetGroupName())
                .vpcSecurityGroupIds(new ArrayList<>(model.getVpcSecurityGroupIds()))
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeEndpointAccessRequest translateToReadRequest(final ResourceModel model) {
        return DescribeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param response the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeEndpointAccessResponse response) {
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L58-L73
        // TODO right now lets just get the first item from the list
        if (response.endpointAccessList().size() == 0) {
            return ResourceModel.builder().build();
        }

        EndpointAccess endpointAccess = response.endpointAccessList().get(0);
        return ResourceModel.builder()
                //.someProperty(response.property())
                .clusterIdentifier(endpointAccess.clusterIdentifier())
                .address(endpointAccess.address())
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteEndpointAccessRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteEndpointAccessRequest.builder()
                .endpointName(model.getEndpointName())
                .clusterIdentifier(model.getClusterIdentifier())
                .vpcId(model.getVpcId())
                .resourceOwner(model.getResourceOwner())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static ModifyEndpointAccessRequest translateToUpdateRequest(final ResourceModel model) {
        return ModifyEndpointAccessRequest.builder()
                .endpointName(model.getEndpointName())
                .clusterIdentifier(model.getClusterIdentifier())
                .vpcId(model.getVpcId())
                .resourceOwner(model.getResourceOwner())
                .vpcSecurityGroupIds(new ArrayList<>(model.getVpcSecurityGroupIds()))
                .build();
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
