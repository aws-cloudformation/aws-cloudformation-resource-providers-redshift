package software.amazon.redshift.endpointauthorization;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.cloudformation.proxy.ProxyClient;

public class Stabilizers {
    public static boolean isAuthorized(
            final ProxyClient<RedshiftClient> proxyClient,
            ResourceModel model,
            CallbackContext cxt) {
        DescribeEndpointAuthorizationRequest request = DescribeEndpointAuthorizationRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .grantee(true) // Call the describe API as the grantee
                .build();

        DescribeEndpointAuthorizationResponse response = proxyClient.injectCredentialsAndInvokeV2(
                request, proxyClient.client()::describeEndpointAuthorization
        );

        if (response.endpointAuthorizationList().isEmpty()) {
            return false;
        }

        // Making the assumption we will only return one value from the call - TODO make sure that is true otherwise
        // This doesn't really work
        return response.endpointAuthorizationList().get(0).status().name().equalsIgnoreCase("authorized");
    }

    public static boolean isDoneRevoking(
            final ProxyClient<RedshiftClient> proxyClient,
            ResourceModel model,
            CallbackContext cxt) {
        DescribeEndpointAuthorizationRequest request = DescribeEndpointAuthorizationRequest.builder()
                .clusterIdentifier(model.getClusterIdentifier())
                .account(model.getAccount())
                .grantee(true) // Call the describe API as the grantee
                .build();

        DescribeEndpointAuthorizationResponse response = proxyClient.injectCredentialsAndInvokeV2(
                request, proxyClient.client()::describeEndpointAuthorization
        );

        // If the output is empty, then we revoked everything, and can return true
        if (response.endpointAuthorizationList().isEmpty()) {
            return true;
        }

        // Make the assumption that we only have one item in the list
        // If the status is revoking, return false
        return !response.endpointAuthorizationList().get(0).status().name().equalsIgnoreCase("revoking");
    }
}
