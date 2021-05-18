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
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validator {
    static String ENDPOINT_NAME_REGEX = "[a-z][a-z0-9]*(-[a-z0-9]+)*";

    static void logFieldsCheck(List<String> requiredFields,
                               ResourceModel resourceModel,
                               String requestType,
                               Logger logger) {
        logger.log(String.format(
                "Checking for required fields: %s within resource model %s for request of type %s",
                requiredFields,
                resourceModel,
                requestType
        ));
    }
    /*
        Validate the fields of the resource model with our API spec.
    */
    static void validateCreateRequest(@NonNull ResourceModel resourceModel, @NonNull Logger logger)
            throws CfnInvalidRequestException {
        String endpointName = resourceModel.getEndpointName();

        logFieldsCheck(Arrays.asList("EndpointName", "SubnetGroupName"), resourceModel, "CREATE", logger);

        // Required fields: EndpointName, SubnetGroupName
        if (isEmpty(endpointName)) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }

        if (isEmpty(resourceModel.getSubnetGroupName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }

        // Validate the endpoint name
        if (!satisfiesEndpointConstraints(endpointName)) {
            logger.log(String.format("EndpointName %s does not match the endpoint name requirements", endpointName));
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static boolean satisfiesEndpointConstraints(String endpointName) {
        return endpointName.matches(ENDPOINT_NAME_REGEX) && endpointName.length() < 30;
    }

    static void validateDeleteRequest(@NonNull ResourceModel resourceModel, @NonNull Logger logger)
            throws CfnInvalidRequestException {
        logFieldsCheck(Arrays.asList("EndpointName"), resourceModel, "DELETE", logger);

        if (isEmpty(resourceModel.getEndpointName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static void validateReadRequest(@NonNull ResourceModel resourceModel, @NonNull Logger logger) {
        logFieldsCheck(Arrays.asList("EndpointName"), resourceModel, "READ", logger);

        if (isEmpty(resourceModel.getEndpointName())) {
            throw new CfnInvalidRequestException(resourceModel.toString());
        }
    }

    static void validateUpdateRequest(@NonNull ProxyClient<RedshiftClient> proxyClient,
                                      @NonNull ResourceModel resourceModel,
                                      @NonNull Logger logger) throws CfnInvalidRequestException {
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

        logFieldsCheck(Arrays.asList("EndpointName"), resourceModel, "UPDATE", logger);

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
        validateCreateOnlyProperties(endpointAccess, resourceModel, logger);
    }

    static void validateCreateOnlyProperties(@NonNull EndpointAccess endpointAccess,
                                             @NonNull ResourceModel resourceModel,
                                             @NonNull Logger logger) {
        // If the parameter from the resource model is null, skip the check, otherwise, check it against the
        // value from the describe call
        HashMap<String, String> propertyValuePairs = buildPropertyValuePairs(endpointAccess, resourceModel);

        if (!createOnlyPropertiesMatch(propertyValuePairs, logger)) {
            throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, resourceModel.getEndpointName());
        }
    }

    static boolean createOnlyPropertiesMatch(HashMap<String, String> propertyValuePairs, Logger logger) {
        boolean createOnlyPropertiesMatch = true;
        for (Map.Entry<String, String> entry: propertyValuePairs.entrySet()) {
            // Validation is only passed when ALL of the validateCreateOnlyProperty calls return TRUE
            createOnlyPropertiesMatch = createOnlyPropertiesMatch &&
                    createOnlyPropertyMatches(entry.getKey(), entry.getValue(), logger);
        }
        return createOnlyPropertiesMatch;
    }

    static HashMap<String, String> buildPropertyValuePairs(EndpointAccess endpointAccess, ResourceModel resourceModel) {
        // Actual value : ResourceModel key value pairs
        // CreateOnlyProperties: EndpointName, SubnetGroupName, ResourceOwner, ClusterIdentifier
        return new HashMap<String, String>() {{
            put(endpointAccess.endpointName(), resourceModel.getEndpointName());
            put(endpointAccess.subnetGroupName(), resourceModel.getSubnetGroupName());
            put(endpointAccess.resourceOwner(), resourceModel.getResourceOwner());
            put(endpointAccess.clusterIdentifier(), resourceModel.getClusterIdentifier());
        }};
    }

    static boolean createOnlyPropertyMatches(@NonNull Object endpointAccessValue,
                                             @Nullable Object resourceValue,
                                             @NonNull Logger logger) {
        if (resourceValue == null) {
            return true;
        }

        boolean modelValueMatchesResourceValue = endpointAccessValue.equals(resourceValue);

        if (!modelValueMatchesResourceValue) {
            logger.log(String.format(
                    "ResourceModel had value %s, which is not equal to actual value of %s",
                    resourceValue,
                    endpointAccessValue)
            );
        }

        return modelValueMatchesResourceValue;
    }

    static boolean isEmpty(@Nullable String string) {
        return StringUtils.isNullOrEmpty(string);
    }
}
