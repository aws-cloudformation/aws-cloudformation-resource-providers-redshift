package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorization;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ValidatorTest {
    @Mock ProxyClient<RedshiftClient> proxyClient;
    @Mock RedshiftClient client;

    String account = "account";
    String clusterIdentifier = "clusterIdentifier";

    @Test
    public void testValidateAuthNotExists() {
        AuthorizeEndpointAccessRequest request = AuthorizeEndpointAccessRequest.builder()
                .account(account)
                .clusterIdentifier(clusterIdentifier)
                .build();

        DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                .endpointAuthorizationList(Arrays.asList(EndpointAuthorization.builder().build()))
                .build();

        doReturn(response).when(proxyClient).injectCredentialsAndInvokeV2(any(), any());
        doReturn(client).when(proxyClient).client();

        assertThrows(CfnAlreadyExistsException.class, () -> Validator.validateAuthNotExists(request, proxyClient));
    }

    @Test
    public void testValidateReadReturnedAuthorization() {
        DescribeEndpointAuthorizationRequest request = DescribeEndpointAuthorizationRequest.builder()
                .account(account)
                .clusterIdentifier(clusterIdentifier)
                .build();

        DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                .endpointAuthorizationList(new ArrayList<EndpointAuthorization>())
                .build();

        assertThrows(CfnNotFoundException.class, () -> Validator.validateReadReturnedAuthorization(request, response));
    }

    @Test
    public void testDoesExist() {
        assertTrue(Validator.doesExist("hi"));
        assertFalse(Validator.doesExist(null));
    }
}
