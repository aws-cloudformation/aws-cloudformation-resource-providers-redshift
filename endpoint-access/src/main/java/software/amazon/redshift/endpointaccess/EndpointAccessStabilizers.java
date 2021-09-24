package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

public class EndpointAccessStabilizers {
    private static final String VALID_ENDPOINT_STATUS = "active";

    public static boolean isEndpointActive(final ProxyClient<RedshiftClient> proxyClient,
                                           ResourceModel model,
                                           CallbackContext cxt) {

        DescribeEndpointAccessRequest request = DescribeEndpointAccessRequest.builder()
                .endpointName(model.getEndpointName())
                .build();

        DescribeEndpointAccessResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeEndpointAccess);

            return response.endpointAccessList()
                    .stream()
                    .findAny()
                    .map(endpointAccess -> endpointAccess.endpointStatus().equalsIgnoreCase(VALID_ENDPOINT_STATUS))
                    .orElse(false);

        } catch (EndpointNotFoundException e) {
            return false;
        }
    }


    public static boolean isEndpointDeleted(final ProxyClient<RedshiftClient> proxyClient,
                                            ResourceModel model,
                                            CallbackContext cxt) {

        DescribeEndpointAccessRequest request = DescribeEndpointAccessRequest.builder()
                .endpointName(model.getEndpointName())
                .build();

        try {
            proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeEndpointAccess);
        } catch (EndpointNotFoundException e) {
            return true;
        }

        return false;
    }
}
