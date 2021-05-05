package software.amazon.redshift.endpointaccess;

import com.amazonaws.util.StringUtils;
import lombok.NonNull;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nullable;

public class Validator {
    static String ENDPOINT_NAME_REGEX = "[a-z][a-z0-9]*(-[a-z0-9]+)*";

    /*
        Validate the fields of the resource model with our API spec.
    */
    static void validateCreateRequest(@NonNull ResourceModel resourceModel) throws CfnInvalidRequestException {
        String endpointName = resourceModel.getEndpointName();
        // Required fields: EndpointName, SubnetGroupName
        if (isEmpty(endpointName)) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }

        if (isEmpty(resourceModel.getSubnetGroupName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }

        // Validate the endpoint name
        if (!endpointName.matches(ENDPOINT_NAME_REGEX) || endpointName.length() > 30) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static void validateDeleteRequest(@NonNull ResourceModel resourceModel) throws CfnInvalidRequestException {
        if (isEmpty(resourceModel.getEndpointName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static void validateReadRequest(@NonNull ResourceModel resourceModel) {
        if (isEmpty(resourceModel.getEndpointName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static void validateUpdateRequest(@NonNull ProxyClient<RedshiftClient> proxyClient,
                                      @NonNull ResourceModel resourceModel) throws CfnInvalidRequestException {
        /*
        1. Endpoint name should be defined (its the primary identifier)
        2. Endpoint should exist
        3. Updates should have the primary identifier, and if there are any createOnlyProperties, these
        should NOT be different than the previous value.


        These are:
            - EndpointName
            - SubnetGroupName
            - ResourceOwner
            - ClusterIdentifier
        */


        String endpointName = resourceModel.getEndpointName();
        if (isEmpty(endpointName)) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }

        DescribeEndpointAccessRequest describeEndpointAccessRequest = DescribeEndpointAccessRequest.builder()
                .endpointName(endpointName)
                .build();

        DescribeEndpointAccessResponse describeEndpointAccessResponse;

        try {
            describeEndpointAccessResponse = proxyClient.injectCredentialsAndInvokeV2(
                    describeEndpointAccessRequest,
                    proxyClient.client()::describeEndpointAccess
            );
        } catch (EndpointNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, endpointName);
        }

        EndpointAccess endpointAccess = describeEndpointAccessResponse.endpointAccessList().get(0);
        validateCreateOnlyProperties(endpointAccess, resourceModel);
    }

    static void validateCreateOnlyProperties(@NonNull EndpointAccess endpointAccess,
                                             @NonNull ResourceModel resourceModel) {
        // If the parameter from the resource model is null, skip the check, otherwise, check it against the
        // value from the describe call
        if (!validateCreateOnlyProperty(endpointAccess.endpointName(), resourceModel.getEndpointName()) ||
                !validateCreateOnlyProperty(endpointAccess.subnetGroupName(), resourceModel.getSubnetGroupName()) ||
                !validateCreateOnlyProperty(endpointAccess.resourceOwner(), resourceModel.getResourceOwner()) ||
                !validateCreateOnlyProperty(endpointAccess.clusterIdentifier(), resourceModel.getClusterIdentifier())) {

            throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, resourceModel.getEndpointName());
        }
    }

    static boolean validateCreateOnlyProperty(@NonNull Object endpointAccessValue,
                                              @Nullable Object resourceValue) {
        if (resourceValue == null) {
            return true;
        }

        return endpointAccessValue.equals(resourceValue);
    }

    static boolean isEmpty(@Nullable String string) {
        return StringUtils.isNullOrEmpty(string);
    }
}
