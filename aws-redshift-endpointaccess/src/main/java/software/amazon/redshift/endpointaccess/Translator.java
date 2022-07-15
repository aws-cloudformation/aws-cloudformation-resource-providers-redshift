package software.amazon.redshift.endpointaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

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
                .vpcSecurityGroupIds(copyList(model.getVpcSecurityGroupIds()))
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
        return response.endpointAccessList()
                .stream()
                .map(endpointAccess -> ResourceModel.builder()
                        .clusterIdentifier(endpointAccess.clusterIdentifier())
                        .resourceOwner(endpointAccess.resourceOwner())
                        .endpointName(endpointAccess.endpointName())
                        .subnetGroupName(endpointAccess.subnetGroupName())
                        .vpcSecurityGroupIds(endpointAccess.vpcSecurityGroups()
                                .stream()
                                .map(VpcSecurityGroupMembership::vpcSecurityGroupId)
                                .collect(Collectors.toList()))
                        .endpointStatus(endpointAccess.endpointStatus())
                        .endpointCreateTime(endpointAccess.endpointCreateTime().toString())
                        .port(endpointAccess.port())
                        .address(endpointAccess.address())
                        .vpcSecurityGroups(endpointAccess.vpcSecurityGroups()
                                .stream()
                                .map(vpcSecurityGroupMembership -> VpcSecurityGroup.builder()
                                        .vpcSecurityGroupId(vpcSecurityGroupMembership.vpcSecurityGroupId())
                                        .status(vpcSecurityGroupMembership.status())
                                        .build())
                                .collect(Collectors.toList()))
                        .vpcEndpoint(translateToModelVpcEndpoint(endpointAccess.vpcEndpoint()))
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
                .vpcSecurityGroupIds(copyList(model.getVpcSecurityGroupIds()))
                .build();
    }

    static ModifyEndpointAccessResponse translateToUpdateResponse(final DescribeEndpointAccessResponse describeEndpointAccessResponse) {
        return describeEndpointAccessResponse.endpointAccessList()
                .stream()
                .findAny()
                .map(endpointAccess -> ModifyEndpointAccessResponse.builder()
                        .clusterIdentifier(endpointAccess.clusterIdentifier())
                        .resourceOwner(endpointAccess.resourceOwner())
                        .subnetGroupName(endpointAccess.subnetGroupName())
                        .endpointStatus(endpointAccess.endpointStatus())
                        .endpointName(endpointAccess.endpointName())
                        .endpointCreateTime(endpointAccess.endpointCreateTime())
                        .port(endpointAccess.port())
                        .address(endpointAccess.address())
                        .vpcSecurityGroups(endpointAccess.vpcSecurityGroups())
                        .vpcEndpoint(endpointAccess.vpcEndpoint())
                        .build())
                .orElse(ModifyEndpointAccessResponse.builder().build());
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

    private static List<String> copyList(List<String> originalList) {
        if (originalList == null) {
            return null;
        }

        return new ArrayList<>(originalList);
    }

    private static VpcEndpoint translateToModelVpcEndpoint(software.amazon.awssdk.services.redshift.model.VpcEndpoint vpcEndpoint) {
        return VpcEndpoint.builder()
                .vpcId(vpcEndpoint.vpcId())
                .vpcEndpointId(vpcEndpoint.vpcEndpointId())
                .networkInterfaces(vpcEndpoint.networkInterfaces()
                        .stream()
                        .map(Translator::translateToModelNetworkInterface)
                        .collect(Collectors.toList()))
                .build();
    }

    private static NetworkInterface translateToModelNetworkInterface(software.amazon.awssdk.services.redshift.model.NetworkInterface networkInterface) {
        return NetworkInterface.builder()
                .availabilityZone(networkInterface.availabilityZone())
                .networkInterfaceId(networkInterface.networkInterfaceId())
                .privateIpAddress(networkInterface.privateIpAddress())
                .subnetId(networkInterface.subnetId())
                .build();
    }
}
