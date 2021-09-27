package software.amazon.redshift.endpointauthorization;

import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;

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
                .vpcIds(model.getVpcIds())
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
                .build();
    }

    /**
     * Request to authorize VpcIds for a resource in Update workflow
     *
     * @param desiredResourceState the resource model request to update VpcIds
     * @param currentResourceState the resource model request to delete VpcIds
     * @return awsRequest the aws service request to authorize VpcIds of a resource
     */
    static AuthorizeEndpointAccessRequest translateToUpdateAuthorizeRequest(final ResourceModel desiredResourceState,
                                                                            final ResourceModel currentResourceState) {
        List<String> toBeAuthorizedVpcIds = desiredResourceState.getVpcIds()
                .stream()
                .filter(vpcId -> !currentResourceState.getVpcIds().contains(vpcId))
                .collect(Collectors.toList());

        if (currentResourceState.getAllowedAllVPCs() && desiredResourceState.getAllowedAllVPCs()) {
            // Return an empty request if the current setting is also allowing-all-VPCs (the VpcIds field is empty).
            return AuthorizeEndpointAccessRequest.builder().build();

        } else if (desiredResourceState.getAllowedAllVPCs()) {
            // Return an authorize-all request if the desired endpoint authorization setting is allowing-all-VPCs (the VpcIds field is empty).
            return AuthorizeEndpointAccessRequest.builder()
                    .clusterIdentifier(currentResourceState.getClusterIdentifier())
                    .account(currentResourceState.getAccount())
                    .build();

        } else if (toBeAuthorizedVpcIds.isEmpty()) {
            // Return an empty request if there is nothing to be authorized.
            return AuthorizeEndpointAccessRequest.builder().build();

        } else {
            // Return a request with toBeAuthorizedVpcIds.
            return AuthorizeEndpointAccessRequest.builder()
                    .clusterIdentifier(currentResourceState.getClusterIdentifier())
                    .account(currentResourceState.getAccount())
                    .vpcIds(toBeAuthorizedVpcIds)
                    .build();
        }
    }

    /**
     * Request to revoke VpcIds for a resource in Update workflow
     *
     * @param desiredResourceState the resource model request to update VpcIds
     * @param currentResourceState the resource model request to delete VpcIds
     * @param force                the "force" setting for revoking endpoint authorization
     * @return awsRequest the aws service request to revoke VpcIds of a resource
     */
    static RevokeEndpointAccessRequest translateToUpdateRevokeRequest(final ResourceModel desiredResourceState,
                                                                      final ResourceModel currentResourceState,
                                                                      final boolean force) {
        List<String> toBeRevokedVpcIds = currentResourceState.getVpcIds()
                .stream()
                .filter(vpcId -> !desiredResourceState.getVpcIds().contains(vpcId))
                .collect(Collectors.toList());

        if (currentResourceState.getAllowedAllVPCs() && desiredResourceState.getAllowedAllVPCs()) {
            // Return an empty request if the desire request is also allowing-all-VPCs (the VpcIds field is empty).
            return RevokeEndpointAccessRequest.builder().build();

        } else if (currentResourceState.getAllowedAllVPCs()) {
            // Return a revoke-all request if the current endpoint authorization setting is allowing-all-VPCs (the VpcIds field is empty).
            return RevokeEndpointAccessRequest.builder()
                    .clusterIdentifier(currentResourceState.getClusterIdentifier())
                    .account(currentResourceState.getAccount())
                    .force(force)
                    .build();

        } else if (toBeRevokedVpcIds.isEmpty()) {
            // Return an empty request if there is nothing to be revoked.
            return RevokeEndpointAccessRequest.builder().build();

        } else {
            // Return a request with toBeRevokedVpcIds.
            return RevokeEndpointAccessRequest.builder()
                    .clusterIdentifier(currentResourceState.getClusterIdentifier())
                    .account(currentResourceState.getAccount())
                    .vpcIds(toBeRevokedVpcIds)
                    .force(force)
                    .build();
        }
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param response the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeEndpointAuthorizationResponse response) {
        return response.endpointAuthorizationList()
                .stream()
                .map(endpointAuthorization -> ResourceModel.builder()
                        .grantor(endpointAuthorization.grantor())
                        .grantee(endpointAuthorization.grantee())
                        .clusterIdentifier(endpointAuthorization.clusterIdentifier())
                        .authorizeTime(endpointAuthorization.authorizeTime().toString())
                        .clusterStatus(endpointAuthorization.clusterStatus())
                        .status(endpointAuthorization.statusAsString())
                        .allowedAllVPCs(endpointAuthorization.allowedAllVPCs())
                        .allowedVPCs(endpointAuthorization.allowedVPCs())
                        .endpointCount(endpointAuthorization.endpointCount())
                        .account(endpointAuthorization.grantee())
                        .vpcIds(endpointAuthorization.allowedVPCs())
                        .build())
                .findAny()
                .orElse(ResourceModel.builder().build());
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
                .vpcIds(model.getVpcIds())
                .force(model.getForce())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeEndpointAuthorizationRequest translateToListRequest(final String nextToken) {
        return DescribeEndpointAuthorizationRequest.builder()
                .marker(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param response the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final DescribeEndpointAuthorizationResponse response) {
        return streamOfOrEmpty(response.endpointAuthorizationList())
                .map(endpointAuthorization -> ResourceModel.builder()
                        .clusterIdentifier(endpointAuthorization.clusterIdentifier())
                        .account(endpointAuthorization.grantee())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
