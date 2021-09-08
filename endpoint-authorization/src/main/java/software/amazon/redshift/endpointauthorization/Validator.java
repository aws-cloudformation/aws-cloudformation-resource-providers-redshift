package software.amazon.redshift.endpointauthorization;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nullable;

public class Validator {
    static void validateAuthNotExists(final AuthorizeEndpointAccessRequest request,
                                         final ProxyClient<RedshiftClient> proxyClient) {
        // The API will not throw an easily parsable error if the endpoint auth already exists.
        // Here we will do a manual check and throw the CfnAlreadyExistsError.

        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(request.account())
                .clusterIdentifier(request.clusterIdentifier())
                .build();

        try {
            DescribeEndpointAuthorizationResponse describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                    describeRequest, proxyClient.client()::describeEndpointAuthorization
            );

            if (!describeResponse.endpointAuthorizationList().isEmpty()) {
                throw new CfnAlreadyExistsException(
                        ResourceModel.TYPE_NAME,
                        String.format("account:%s-clusteridentifier:%s",
                                request.account(), request.clusterIdentifier())
                );
            }
        } catch (CfnAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            // If anything happened, we can just return false (does not exist). The error checking for cluster id
            // etc should be at the create level.
        }
    }

    static void validateReadReturnedAuthorization(DescribeEndpointAuthorizationRequest request,
                                                  DescribeEndpointAuthorizationResponse response) {
        if (response.endpointAuthorizationList().isEmpty()) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    String.format("account%s-clusteridentifier%s-auth",
                            request.account(),
                            request.clusterIdentifier())
            );
        }
    }

    static boolean doesExist(@Nullable String string) {
        return !StringUtils.isNullOrEmpty(string);
    }
}
