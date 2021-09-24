package software.amazon.redshift.endpointaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.NetworkInterface;
import software.amazon.awssdk.services.redshift.model.VpcEndpoint;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    String clusterIdentifier = "clusterId";
    String resourceOwner = "resourceOwner";
    String endpointName = "endpointName";
    String subnetGroupName = "subnetGroupName";
    String endpointStatus = "endpointStatus";
    Instant endpointCreateTime = Instant.now();
    Integer port = 22;
    String address = "address";
    String vpcSecurityGroupId = "vpcSecurityGroupId";
    String vpcSecurityGroupStatus = "status";
    List<VpcSecurityGroupMembership> securityGroupMemberships = Collections.singletonList(VpcSecurityGroupMembership.builder()
            .status(vpcSecurityGroupStatus)
            .vpcSecurityGroupId(vpcSecurityGroupId)
            .build()
    );
    String vpcEndpointId = "vpcEndpointId";
    String vpcId = "vpcId";
    String networkInterfaceId = "networkInterfaceId";
    String subnetId = "subnetId";
    String privateIdAddress = "privateIpAddress";
    String az = "az";
    List<NetworkInterface> networkInterfaces = Collections.singletonList(
            NetworkInterface.builder()
                    .subnetId(subnetId)
                    .networkInterfaceId(networkInterfaceId)
                    .privateIpAddress(privateIdAddress)
                    .availabilityZone(az)
                    .build()
    );

    List<software.amazon.redshift.endpointaccess.NetworkInterface> resourceModelNetworkInterfaces = Collections.singletonList(
            software.amazon.redshift.endpointaccess.NetworkInterface.builder()
                    .networkInterfaceId(networkInterfaceId)
                    .subnetId(subnetId)
                    .privateIpAddress(privateIdAddress)
                    .availabilityZone(az)
                    .build()
    );

     VpcEndpoint vpcEndpoint = VpcEndpoint.builder()
            .networkInterfaces(networkInterfaces)
            .vpcEndpointId(vpcEndpointId)
            .vpcId(vpcId)
            .build();

     software.amazon.redshift.endpointaccess.VpcEndpoint resourceModelVpcEndpoint =
             software.amazon.redshift.endpointaccess.VpcEndpoint.builder()
                     .vpcEndpointId(vpcEndpointId)
                     .vpcId(vpcId)
                     .networkInterfaces(resourceModelNetworkInterfaces)
                     .build();

    List<String> vpcSecurityGroupIds = Collections.singletonList(vpcSecurityGroupId);

    EndpointAccess endpointAccess = EndpointAccess.builder()
            .clusterIdentifier(clusterIdentifier)
            .resourceOwner(resourceOwner)
            .subnetGroupName(subnetGroupName)
            .endpointStatus(endpointStatus)
            .endpointName(endpointName)
            .endpointCreateTime(endpointCreateTime)
            .port(port)
            .address(address)
            .vpcSecurityGroups(securityGroupMemberships)
            .vpcEndpoint(vpcEndpoint)
            .build();

    List<EndpointAccess> endpointAccessList = Collections.singletonList(endpointAccess);
    List<VpcSecurityGroup> vpcSecurityGroups = Collections.singletonList(
            VpcSecurityGroup.builder()
                    .vpcSecurityGroupId(vpcSecurityGroupId)
                    .status(vpcSecurityGroupStatus)
                    .build()
    );

    ResourceModel resourceModel = ResourceModel.builder()
            .clusterIdentifier(clusterIdentifier)
            .resourceOwner(resourceOwner)
            .endpointName(endpointName)
            .subnetGroupName(subnetGroupName)
            .vpcSecurityGroupIds(new ArrayList<>(vpcSecurityGroupIds))
            .build();

    @Test
    public void testTranslateToCreateRequest() {
        CreateEndpointAccessRequest request = CreateEndpointAccessRequest.builder()
                .clusterIdentifier(clusterIdentifier)
                .resourceOwner(resourceOwner)
                .endpointName(endpointName)
                .subnetGroupName(subnetGroupName)
                .vpcSecurityGroupIds(new ArrayList<>(vpcSecurityGroupIds))
                .build();

        assertEquals(request, Translator.translateToCreateRequest(resourceModel));
    }

    @Test
    public void testTranslateToReadRequest() {
        DescribeEndpointAccessRequest request = DescribeEndpointAccessRequest.builder()
                .endpointName(endpointName)
                .build();

        assertEquals(request, Translator.translateToReadRequest(resourceModel));
    }

    @Test
    public void testTranslateToDeleteRequest() {
        DeleteEndpointAccessRequest request = DeleteEndpointAccessRequest.builder()
                .endpointName(endpointName)
                .build();

        assertEquals(request, Translator.translateToDeleteRequest(resourceModel));
    }

    @Test
    public void testTranslateToListRequest() {
        String nextToken = "next token";
        DescribeEndpointAccessRequest request = DescribeEndpointAccessRequest.builder()
                .marker(nextToken)
                .build();

        assertEquals(request, Translator.translateToListRequest(nextToken));
    }

    @Test
    public void testTranslateFromReadResponse() {
        DescribeEndpointAccessResponse response = DescribeEndpointAccessResponse.builder()
                .endpointAccessList(endpointAccessList)
                .build();

        ResourceModel expectedResourceModel = ResourceModel.builder()
                .clusterIdentifier(clusterIdentifier)
                .resourceOwner(resourceOwner)
                .endpointName(endpointName)
                .subnetGroupName(subnetGroupName)
                .vpcSecurityGroupIds(vpcSecurityGroupIds)
                .endpointStatus(endpointStatus)
                .endpointCreateTime(endpointCreateTime.toString())
                .port(port)
                .address(address)
                .vpcSecurityGroups(vpcSecurityGroups)
                .vpcEndpoint(resourceModelVpcEndpoint)
                .build();

        assertEquals(expectedResourceModel, Translator.translateFromReadResponse(response));
    }

    @Test
    public void testTranslateFromEmptyReadResponse() {
        DescribeEndpointAccessResponse response = DescribeEndpointAccessResponse.builder()
                .endpointAccessList(new ArrayList<>())
                .build();

        assertEquals(ResourceModel.builder().build(), Translator.translateFromReadResponse(response));
    }
}
