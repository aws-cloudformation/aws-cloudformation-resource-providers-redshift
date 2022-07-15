package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

public class EndpointAccessStabilizers {
    public static boolean isEndpointActive(final ProxyClient<RedshiftClient> proxyClient,
                                           ResourceModel model,
                                           CallbackContext cxt) {

        DescribeEndpointAccessRequest request = DescribeEndpointAccessRequest.builder()
                .endpointName(model.getEndpointName())
                .build();

        DescribeEndpointAccessResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeEndpointAccess);
        } catch (EndpointNotFoundException e) {
            return false;
        }

        if (response.endpointAccessList().isEmpty()) {
            return false;
        }

        return response.endpointAccessList().get(0).endpointStatus().equalsIgnoreCase("active");
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
