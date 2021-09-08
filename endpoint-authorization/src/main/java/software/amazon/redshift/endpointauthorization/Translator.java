package software.amazon.redshift.endpointauthorization;

import com.amazonaws.util.StringUtils;
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
     * @return awsRequest the aws service request to create a resource
     */
    static AuthorizeEndpointAccessRequest translateToCreateRequest(final ResourceModel model) {
        AuthorizeEndpointAccessRequest.Builder requestBuilder = AuthorizeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount());

        if (model.getVpcIds() != null && !model.getVpcIds().isEmpty()) {
            requestBuilder.vpcIds(model.getVpcIds());
        }

        return requestBuilder.build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeEndpointAuthorizationRequest translateToReadRequest(final ResourceModel model) {
        String targetAccount = model.getAccount();
        Boolean asGrantee = Optional.ofNullable(model.getAsGrantee()).orElse(false);

        if (StringUtils.isNullOrEmpty(model.getAccount())) {
            // What if we did a read right after a create? The account should be grantor
            if (asGrantee) {
                targetAccount = model.getGrantor();
            } else {
                targetAccount = model.getGrantee();
            }
        }

        return DescribeEndpointAuthorizationRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(targetAccount)
                .grantee(asGrantee)
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

        List<String> vpcIdsInUpdateRequest = model.getVpcIds();

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
        List<String> vpcIdsInUpdateRequest = model.getVpcIds();

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
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    String.format("account:%s-clusteridentifier:%s",
                            accountId,
                            clusterId)
            );
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
        List<EndpointAuthorization> endpointAuthorizationList = response.endpointAuthorizationList();
        if (endpointAuthorizationList.isEmpty()) {
            return ResourceModel.builder().build();
        }

        Function<Function<EndpointAuthorization, Object>, Object> producer = buildProducer(endpointAuthorizationList);
        Function<Function<EndpointAuthorization, List<String>>, List<String>> listProducer = buildListProducer(endpointAuthorizationList);

        return ResourceModel.builder()
                .grantor((String) producer.apply(EndpointAuthorization::grantor))
                .grantee((String) producer.apply(EndpointAuthorization::grantee))
                .clusterIdentifier((String) producer.apply(EndpointAuthorization::clusterIdentifier))
                .clusterStatus((String) producer.apply(EndpointAuthorization::clusterStatus))
                .status(producer.apply(EndpointAuthorization::status).toString())
                .allowedAllVPCs((Boolean) producer.apply(EndpointAuthorization::allowedAllVPCs))
                .vpcIds(listProducer.apply(EndpointAuthorization::allowedVPCs))
                .endpointCount((Integer) producer.apply(EndpointAuthorization::endpointCount))
                .build();
    }

    /*
        Builds a function given the endpointAccessList, which you call with a single parameter - the resource's
        getter method, and will return either that resource, or null.
    */
    private static Function<Function<EndpointAuthorization, Object>, Object> buildProducer(
            final Collection<EndpointAuthorization> endpointAuthorizationList) {
        return (function) -> getResourceOptional(endpointAuthorizationList, function).orElse(null);
    }

    private static <T> Function<Function<EndpointAuthorization, List<T>>, List<T>> buildListProducer(
            final Collection<EndpointAuthorization> endpointAuthorizationList) {
        return (function) -> getResourceOptional(endpointAuthorizationList, function).orElse(null);
    }

    /*
        Our get API returns a list of objects regardless of whether we specified a single one or not.

        This function:
            - Takes in the list of objects (EndpointAuthorizations')
            - Builds a stream of single items from each EndpointAuthorization, getting that item using the getter method
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
     * Request to update some other properties that could not be provisioned through first update request
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static RevokeEndpointAccessRequest translateToRevokeRequest(final ResourceModel model) {
        String account = model.getAccount();

        // Revoke is called by the grantor
        if (StringUtils.isNullOrEmpty(account)) {
            account = model.getGrantee();
        }

        RevokeEndpointAccessRequest.Builder builder = RevokeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(account)
                .force(model.getForce());


        List<String> vpcIds = model.getVpcIds();
        if (vpcIds != null && !vpcIds.isEmpty()) {
            builder.vpcIds(vpcIds);
        }

        return builder.build();
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
                        .grantee(endpointAuthorization.grantee())
                        .grantor(endpointAuthorization.grantor())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
