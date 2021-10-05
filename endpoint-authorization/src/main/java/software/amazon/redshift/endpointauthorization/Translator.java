package software.amazon.redshift.endpointauthorization;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorization;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Collection;
import java.util.Collections;
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
     * Translates resource model into an AuthorizeEndpointAccess request, for the update use case
     */
    static AuthorizeEndpointAccessRequest translateToUpdateAuthorizeRequest(
            final ResourceModel model,
            final ProxyClient<RedshiftClient> proxyClient) {

        List<String> vpcIdsToAdd = getVpcIdsToAdd(model, proxyClient);

        // If we returned null, then we should skip this step
        if (vpcIdsToAdd == null) {
            return null;
        }


        return AuthorizeEndpointAccessRequest.builder()
                .vpcIds(vpcIdsToAdd)
                .account(model.getAccount())
                .clusterIdentifier(model.getClusterIdentifier())
                .build();
    }

    static RevokeEndpointAccessRequest translateToUpdateRevokeRequest(
            final ResourceModel model,
            final ProxyClient<RedshiftClient> proxyClient) {
        List<String> vpcIdsToRemove = getVpcIdsToRemove(model, proxyClient);

        // Skip this step if we returned null
        if (vpcIdsToRemove == null) {
            return null;
        }

        return RevokeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .vpcIds(vpcIdsToRemove)
                .build();
    }


    static List<String> getVpcIdsToAdd(ResourceModel model, ProxyClient<RedshiftClient> proxyClient) {
        // Get the list of existing VPC Ids in the Authorization
        List<String> existingVpcIds = getExistingVpcIds(
                model.getAccount(),
                model.getClusterIdentifier(),
                proxyClient
        );

        List<String> vpcIdsInUpdateRequest = model.getVpcIds() == null ? Collections.emptyList() : model.getVpcIds();

        // If there are no current VPC ids specified in the authorization, this means all VPCs are authorized
        if (existingVpcIds.isEmpty()) {
            // If there are any number of VPCs in the authorize request, then we are trying to
            // authorize all -> authorize specific, which is currently not supported
            if (!vpcIdsInUpdateRequest.isEmpty()) {
                throw new CfnInvalidRequestException(model.toString());
            }

            // Otherwise, if there were no VPC ids in the update request, then the operation is idempotent
            return null;
        }

        // Send an empty list of we want to authorize all. Otherwise, determine what VPCs to authorize
        List<String> vpcIdsToSend = vpcIdsInUpdateRequest.isEmpty() ?
                Collections.emptyList() :
                getVpcIdsToAdd(existingVpcIds, vpcIdsInUpdateRequest);

        // If the update request contained VPC ids, but there are no VPC ids to add to the auth, then we should
        // skip the create call, since the VPC ids in the update request are just those the customer is trying to
        // revoke.
        if (vpcIdsToSend.isEmpty() && !vpcIdsInUpdateRequest.isEmpty()) {
            // If we have no vpcs to add to the auth, but the request was not an empty one, then
            // we should not call the auth api with an empty list (this would result in an auth all)
            return null;

        }

        return vpcIdsToSend;
    }

    static List<String> getVpcIdsToRemove(ResourceModel model, ProxyClient<RedshiftClient> proxyClient) {
        List<String> existingVpcIds = getExistingVpcIds(
                model.getAccount(),
                model.getClusterIdentifier(),
                proxyClient
        );
        List<String> vpcIdsInUpdateRequest = model.getVpcIds() == null ? Collections.emptyList() : model.getVpcIds();

        // This means that we are trying to authorize specific after doing an authorize all - not allowed
        if (existingVpcIds.isEmpty()) {
            // If we had authorized all before, we cannot go from auth all -> auth specific yet
            if (!vpcIdsInUpdateRequest.isEmpty()) {
                throw new CfnInvalidRequestException(model.toString() + existingVpcIds);
            }
            // Otherwise, we are trying to do an idempotent update?
            return null;
        }

        List<String> vpcIdsToRemove = getVpcIdsToRemove(existingVpcIds, vpcIdsInUpdateRequest);
        // If there were no vpcs to remove
        if (vpcIdsToRemove.isEmpty()) {
            return null;
        }

        return vpcIdsToRemove;
    }

    static List<String> getVpcIdsToAdd(List<String> existingVpcIds, List<String> vpcIdsInUpdateRequest) {
        // Return all IDs in the update request that are not in the existing auth
        return vpcIdsInUpdateRequest.stream()
                .filter(vpcId -> !existingVpcIds.contains(vpcId))
                .collect(Collectors.toList());
    }

    static List<String> getVpcIdsToRemove(List<String> existingVpcIds, List<String> vpcIdsInUpdateRequest) {
        // We are removing a VPC if if it is in the existingVpcIds list, but not in the vpcIdsInUpdateRequest
        return existingVpcIds.stream()
                .filter(vpcId -> !vpcIdsInUpdateRequest.contains(vpcId))
                .collect(Collectors.toList());
    }

    static List<String> getExistingVpcIds(final String accountId,
                                          final String clusterId,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        // Make a call to check the existing VPC ids that exist for the auth. Remove the ones that already
        // exist before making the API call, otherwise we get an error saying vpc id already exists.
        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(accountId)
                .clusterIdentifier(clusterId)
                .build();

        DescribeEndpointAuthorizationResponse describeResponse = null;


        describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                describeRequest, proxyClient.client()::describeEndpointAuthorization
        );

        List<EndpointAuthorization> endpointAuthorizationList = describeResponse.endpointAuthorizationList();
        if (endpointAuthorizationList.isEmpty()) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, String.format("%s|%s", clusterId, accountId));
        }

        return endpointAuthorizationList.get(0).allowedVPCs();
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
