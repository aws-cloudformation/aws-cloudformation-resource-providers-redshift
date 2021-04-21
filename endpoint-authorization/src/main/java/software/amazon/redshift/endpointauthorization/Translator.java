package software.amazon.redshift.endpointauthorization;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorization;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.cloudformation.proxy.ProxyClient;

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
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static AuthorizeEndpointAccessRequest translateToUpdateRequest(final ResourceModel model,
                                                                   final ProxyClient<RedshiftClient> proxyClient) {
        // If we are trying to modify the authorization, we use the authorize API to modify the authorized vpcs.
        // Note that right now we cannot go from allow all -> allow a certain one. So if allow all is true,
        // the update request should fail (unless it is trying to set allow all).


        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(model.getAccount())
                .clusterIdentifier(model.getClusterIdentifier())
                .build();
        DescribeEndpointAuthorizationResponse describeResponse = null;
        try {
            describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                    describeRequest, proxyClient.client()::describeEndpointAuthorization
            );

        } catch (Exception e) {
            // If anything happened, we can just return false (does not exist). The error checking for cluster id
            // etc should be at the create level.
        }

        List<String> existingVpcIds = describeResponse.endpointAuthorizationList().get(0).allowedVPCs();


        AuthorizeEndpointAccessRequest.Builder requestBuilder = AuthorizeEndpointAccessRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount());


        if (model.getVpcIds() != null && !model.getVpcIds().isEmpty()) {
            List<String> vpcIdsToSend = model.getVpcIds();
            existingVpcIds.forEach(vpcIdsToSend::remove);
            requestBuilder.vpcIds(vpcIdsToSend);
        }

        return requestBuilder.build();
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
