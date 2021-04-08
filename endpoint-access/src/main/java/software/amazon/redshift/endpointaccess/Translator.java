package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
                .endpointName(model.getEndpointName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param response the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeEndpointAccessResponse response) {
        List<EndpointAccess> endpointAccessList = response.endpointAccessList();
        if (endpointAccessList.size() == 0) {
            return ResourceModel.builder().build();
        }

        Function<Function<EndpointAccess, Object>, Object> producer = buildProducer(endpointAccessList);
        Function<Function<EndpointAccess, List<VpcSecurityGroup>>, List<VpcSecurityGroup>> listProducer =
                buildListProducer(endpointAccessList);

        return ResourceModel.builder()
                .clusterIdentifier((String) producer.apply(EndpointAccess::clusterIdentifier))
                .resourceOwner((String) producer.apply(EndpointAccess::resourceOwner))
                .subnetGroupName((String) producer.apply(EndpointAccess::subnetGroupName))
                .endpointStatus((String) producer.apply(EndpointAccess::endpointStatus))
                .endpointName((String) producer.apply(EndpointAccess::endpointName))
                .endpointCreateTime(producer.apply(EndpointAccess::endpointCreateTime).toString())
                .port((Integer) producer.apply(EndpointAccess::port))
                .address((String) producer.apply(EndpointAccess::address))
                .vpcSecurityGroups(listProducer.apply(Translator::buildSecurityGroupList))
                .vpcEndpoint((VpcEndpoint) producer.apply(Translator::buildVpcEndpoint))
                .build();
    }

    private static VpcEndpoint buildVpcEndpoint(EndpointAccess endpointAccess) {
        software.amazon.awssdk.services.redshift.model.VpcEndpoint vpcEndpointSO = endpointAccess.vpcEndpoint();
        return VpcEndpoint.builder()
                .vpcId(vpcEndpointSO.vpcId())
                .vpcEndpointId(vpcEndpointSO.vpcEndpointId())
                .networkInterfaces(vpcEndpointSO.networkInterfaces()
                        .stream()
                        .map(Translator::buildNetworkInterface)
                        .collect(Collectors.toList()))
                .build();
    }

    private static NetworkInterface buildNetworkInterface(
            software.amazon.awssdk.services.redshift.model.NetworkInterface networkInterfaceSO) {
        return NetworkInterface.builder()
                .availabilityZone(networkInterfaceSO.availabilityZone())
                .networkInterfaceId(networkInterfaceSO.networkInterfaceId())
                .privateIpAddress(networkInterfaceSO.privateIpAddress())
                .subnetId(networkInterfaceSO.subnetId())
                .build();
    }

    private static List<VpcSecurityGroup> buildSecurityGroupList(EndpointAccess endpointAccess) {
        return endpointAccess.vpcSecurityGroups().stream()
                .map(Translator::buildVpcSecurityGroup)
                .collect(Collectors.toList());
    }

    private static VpcSecurityGroup buildVpcSecurityGroup(VpcSecurityGroupMembership membership) {
        return VpcSecurityGroup.builder()
                .vpcSecurityGroupId(membership.vpcSecurityGroupId())
                .status(membership.status())
                .build();
    }

    /*
        Builds a function given the endpointAccessList, which you call with a single parameter - the resource's
        getter method, and will return either that resource, or null.
     */
    private static Function<Function<EndpointAccess, Object>, Object> buildProducer(
            final Collection<EndpointAccess> endpointAccessList) {
        return (function) -> getResourceOptional(endpointAccessList, function).orElse(null);
    }

    private static <T> Function<Function<EndpointAccess, List<T>>, List<T>> buildListProducer(
            final Collection<EndpointAccess> endpointAccessList) {
        return (function) -> getResourceOptional(endpointAccessList, function).orElse(null);
    }

    /*
        Our get API returns a list of objects regardless of whether we specified a single one or not.

        This function:
            - Takes in the list of objects (EndpointAccess')
            - Builds a stream of single items from each EndpointAccess, getting that item using the getter method
            - Returns any of these, if at least one exists (it should only contain one), otherwise returns null
     */
    private static <U, T> Optional<U> getResourceOptional(final Collection<T> resourceList,
                                                          Function<? super T, ? extends U> resourceGetterMethod) {
        return Optional.ofNullable(streamOfOrEmpty(resourceList)
                .map(resourceGetterMethod)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null));
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
     * @return describeEndpointAccessRequest the aws service request to list resources within aws account
     */
    static DescribeEndpointAccessRequest translateToListRequest(final String nextToken) {
        return DescribeEndpointAccessRequest.builder()
                .marker(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param response the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final DescribeEndpointAccessResponse response) {
        // List responses only include the primary identifier, which is the endpoint name
        return streamOfOrEmpty(response.endpointAccessList())
                .map(endpointAccess -> ResourceModel.builder()
                        .endpointName(endpointAccess.endpointName())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
