package software.amazon.redshift.endpointauthorization;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizationStatus;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorization;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    public static abstract class TranslatorAbstractTest extends AbstractStaticTest<Translator> {
        Class<Translator> getMockedClass() {
            return Translator.class;
        }
    }

    @Mock ProxyClient<RedshiftClient> proxyClient;
    @Mock RedshiftClient client;

    String clusterIdentifier = "cluster-id";
    String account = "account";
    String vpcId = "vpcId";
    List<String> vpcIds = Arrays.asList(vpcId);

    @Nested
    @DisplayName("TranslateToCreateRequest")
    public class TranslateToCreateRequestTest extends TranslatorAbstractTest {
        ResourceModel resourceModel;
        AuthorizeEndpointAccessRequest expectedRequest;

        @BeforeEach
        public void setup() {
            expectedRequest = AuthorizeEndpointAccessRequest.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .build();

            resourceModel = ResourceModel.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .build();
        }
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToCreateRequest(any());
        }

        @Test
        public void testTranslateAllowSpecific() {
            expectedRequest = expectedRequest.toBuilder().vpcIds(vpcIds).build();
            resourceModel.setVpcIds(vpcIds);

            assertEquals(expectedRequest, Translator.translateToCreateRequest(resourceModel));
        }

        @Test
        public void testTranslateAllowAll() {
            assertEquals(expectedRequest, Translator.translateToCreateRequest(resourceModel));
        }
    }

    @Nested
    @DisplayName("TranslateToReadRequest")
    public class TranslateToReadRequestTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToReadRequest(any());
        }

        DescribeEndpointAuthorizationRequest expectedRequest;

        @BeforeEach
        public void setup() {
            expectedRequest = DescribeEndpointAuthorizationRequest.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .build();
        }

        @Test
        public void testTranslateSimpleCase() {
            ResourceModel resourceModel = ResourceModel.builder()
                    .account(account)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            assertEquals(expectedRequest, Translator.translateToReadRequest(resourceModel));
        }
    }

    @Nested
    @DisplayName("TranslateToUpdateAuthorizeRequest")
    public class TranslateToUpdateAuthorizeRequestTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToUpdateAuthorizeRequest(any(), any());
        }

        ResourceModel resourceModel = ResourceModel.builder()
                .account(account)
                .clusterIdentifier(clusterIdentifier)
                .build();

        @Test
        public void testNullVpcIdsToAdd() {
            mockedClass.when(() -> Translator.getVpcIdsToAdd(resourceModel, proxyClient)).thenReturn(null);
            assertNull(Translator.translateToUpdateAuthorizeRequest(resourceModel, proxyClient));
        }

        @Test
        public void testFoundVpcIdsToAdd() {
            AuthorizeEndpointAccessRequest expectedRequest = AuthorizeEndpointAccessRequest.builder()
                    .vpcIds(vpcIds)
                    .account(account)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            mockedClass.when(() -> Translator.getVpcIdsToAdd(resourceModel, proxyClient)).thenReturn(vpcIds);
            assertEquals(expectedRequest, Translator.translateToUpdateAuthorizeRequest(resourceModel, proxyClient));
        }
    }

    @Nested
    @DisplayName("TranslateToUpdateRevokeRequest")
    public class TranslateToUpdateRevokeRequestTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToUpdateRevokeRequest(any(), any());
        }

        ResourceModel resourceModel = ResourceModel.builder()
                .account(account)
                .clusterIdentifier(clusterIdentifier)
                .build();

        @Test
        public void testNullVpcIdsToRemove() {
            mockedClass.when(() -> Translator.getVpcIdsToRemove(resourceModel, proxyClient)).thenReturn(null);
            assertNull(Translator.translateToUpdateRevokeRequest(resourceModel, proxyClient));
        }

        @Test
        public void testFoundVpcIdsToRemove() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .vpcIds(vpcIds)
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .build();

            mockedClass.when(() -> Translator.getVpcIdsToRemove(resourceModel, proxyClient)).thenReturn(vpcIds);
            assertEquals(expectedRequest, Translator.translateToUpdateRevokeRequest(resourceModel, proxyClient));
        }
    }

    @Nested
    @DisplayName("GetVpcIdsToAdd")
    public class GetVpcIdsToAddTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.getVpcIdsToAdd(any(ResourceModel.class), any());
        }
        @Mock ResourceModel resourceModel;

        @BeforeEach
        public void setup() {
            doReturn(account).when(resourceModel).getAccount();
            doReturn(clusterIdentifier).when(resourceModel).getClusterIdentifier();
        }

        @Test
        public void testIdempotentAllowAll() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(Collections.EMPTY_LIST);
            doReturn(Collections.EMPTY_LIST).when(resourceModel).getVpcIds();

            assertNull(Translator.getVpcIdsToAdd(resourceModel, proxyClient));
        }

        @Test
        public void testAuthorizeAllToSpecificThrowsError() {
             mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                     .thenReturn(Collections.EMPTY_LIST);
             doReturn(vpcIds).when(resourceModel).getVpcIds();

             assertThrows(
                     CfnInvalidRequestException.class,
                     () -> Translator.getVpcIdsToAdd(resourceModel, proxyClient)
             );
        }

        @Test
        public void testNoVpcIdsToSend() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(vpcIds);
            doReturn(vpcIds).when(resourceModel).getVpcIds();

            mockedClass.when(() -> Translator.getVpcIdsToAdd(vpcIds, vpcIds)).thenReturn(Collections.EMPTY_LIST);
            assertNull(Translator.getVpcIdsToAdd(resourceModel, proxyClient));
        }

        @Test
        public void testNoVpcIdsInUpdateRequest() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(vpcIds);
            doReturn(Collections.EMPTY_LIST).when(resourceModel).getVpcIds();

            assertTrue(Translator.getVpcIdsToAdd(resourceModel, proxyClient).isEmpty());
        }
    }

    @Nested
    @DisplayName("GetVpcIdsToRemove")
    public class GetVpcIdsToRemoveTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.getVpcIdsToRemove(any(ResourceModel.class), any());
        }
        @Mock ResourceModel resourceModel;

        @BeforeEach
        public void setup() {
            doReturn(account).when(resourceModel).getAccount();
            doReturn(clusterIdentifier).when(resourceModel).getClusterIdentifier();
        }

        @Test
        public void testIdempotentAllowAll() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(Collections.EMPTY_LIST);
            doReturn(Collections.EMPTY_LIST).when(resourceModel).getVpcIds();

            assertNull(Translator.getVpcIdsToRemove(resourceModel, proxyClient));
        }

        @Test
        public void testAuthorizeAllToSpecificThrowsError() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(Collections.EMPTY_LIST);
            doReturn(vpcIds).when(resourceModel).getVpcIds();

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Translator.getVpcIdsToRemove(resourceModel, proxyClient)
            );
        }

        @Test
        public void testNoVpcIdsToSend() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(vpcIds);
            doReturn(vpcIds).when(resourceModel).getVpcIds();

            mockedClass.when(() -> Translator.getVpcIdsToRemove(vpcIds, vpcIds)).thenReturn(Collections.EMPTY_LIST);
            assertNull(Translator.getVpcIdsToRemove(resourceModel, proxyClient));
        }

        @Test
        public void testNoVpcIdsInUpdateRequest() {
            mockedClass.when(() -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient))
                    .thenReturn(vpcIds);
            doReturn(vpcIds).when(resourceModel).getVpcIds();
            mockedClass.when(() -> Translator.getVpcIdsToRemove(vpcIds, vpcIds)).thenReturn(vpcIds);

            assertEquals(vpcIds, Translator.getVpcIdsToRemove(resourceModel, proxyClient));
        }
    }

    @Test
    public void testGetVpcIdsToAddFilter() {
        List<String> vpcIdsInUpdateRequest = Arrays.asList("id-1", "id-2");
        List<String> existingVpcIds = Arrays.asList("id-1");
        List<String> expectedVpcIds = Arrays.asList("id-2");

        assertEquals(expectedVpcIds, Translator.getVpcIdsToAdd(existingVpcIds, vpcIdsInUpdateRequest));
    }

    @Test
    public void testGetVpcIdsToRemoveFilter() {
        List<String> vpcIdsInUpdateRequest = Arrays.asList("id-2");
        List<String> existingVpcIds = Arrays.asList("id-1", "id-2");
        List<String> expectedVpcIds = Arrays.asList("id-1");

        assertEquals(expectedVpcIds, Translator.getVpcIdsToRemove(existingVpcIds, vpcIdsInUpdateRequest));
    }

    @Nested
    @DisplayName("TranslateFromReadResponse")
    public class TranslateFromReadResponseTest {
        @Test
        public void testTranslateEmptyResponse() {
            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                    .endpointAuthorizationList(new ArrayList<EndpointAuthorization>())
                    .build();

            assertEquals(ResourceModel.builder().build(), Translator.translateFromReadResponse(response));
        }
        @Test
        public void testTranslate() {
            String grantor = "grantor";
            String grantee = "grantee";
            String clusterStatus = "cluster status";
            Instant authorizeTime = Instant.now();
            AuthorizationStatus authorizationStatus = AuthorizationStatus.AUTHORIZED;
            Boolean allowedAllVPCs = RandomUtils.nextBoolean();
            Integer endpointCount = 1;

            EndpointAuthorization endpointAuthorization = EndpointAuthorization.builder()
                    .grantor(grantor)
                    .grantee(grantee)
                    .clusterIdentifier(clusterIdentifier)
                    .authorizeTime(authorizeTime)
                    .clusterStatus(clusterStatus)
                    .status(authorizationStatus)
                    .allowedAllVPCs(allowedAllVPCs)
                    .allowedVPCs(vpcIds)
                    .endpointCount(endpointCount)
                    .build();

            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                    .endpointAuthorizationList(Collections.singletonList(endpointAuthorization))
                    .build();

            ResourceModel expectedResourceModel = ResourceModel.builder()
                    .grantor(grantor)
                    .grantee(grantee)
                    .clusterIdentifier(clusterIdentifier)
                    .clusterStatus(clusterStatus)
                    .authorizeTime(authorizeTime.toString())
                    .status(authorizationStatus.toString())
                    .allowedAllVPCs(allowedAllVPCs)
                    .allowedVPCs(endpointAuthorization.allowedVPCs())
                    .endpointCount(endpointCount)
                    .account(grantee)
                    .vpcIds(vpcIds)
                    .build();

            assertEquals(expectedResourceModel, Translator.translateFromReadResponse(response));
        }
    }

    @Nested
    @DisplayName("TranslateToRevokeRequest")
    public class TranslateToRevokeRequest {
        @Test
        public void testTranslate() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            assertEquals(expectedRequest, Translator.translateToRevokeRequest(model));
        }

        @Test
        public void testTranslateNoVpcIds() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            assertEquals(expectedRequest, Translator.translateToRevokeRequest(model));
        }

        @Test
        public void testTranslateNoAccount() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .account(account)
                    .force(false)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            assertEquals(expectedRequest, Translator.translateToRevokeRequest(model));
        }
    }

    @Test
    public void testTranslateToListRequest() {
        String nextToken = "next token";
        assertEquals(
                DescribeEndpointAuthorizationRequest.builder().marker(nextToken).build(),
                Translator.translateToListRequest(nextToken)
        );
    }

    @Test
    public void testTranslateFromListRequest() {
        EndpointAuthorization endpointAuthorization = EndpointAuthorization.builder()
                .clusterIdentifier(clusterIdentifier)
                .grantee(account)
                .grantor(account)
                .build();

        List<ResourceModel> expectedList = Arrays.asList(
                ResourceModel.builder()
                        .clusterIdentifier(clusterIdentifier)
                        .account(account)
                        .build()
        );

        DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                .endpointAuthorizationList(Arrays.asList(endpointAuthorization))
                .build();

        assertEquals(expectedList, Translator.translateFromListRequest(response));
    }

    @Nested
    @DisplayName("GetExistingVpcIds")
    public class GetExistingVpcIdsTest {
        @BeforeEach
        public void setup() {
            doReturn(client).when(proxyClient).client();
        }

        @Test
        public void testThrowsExceptionOnEmptyList() {
            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                    .endpointAuthorizationList(new ArrayList<EndpointAuthorization>())
                    .build();

            doReturn(response).when(proxyClient).injectCredentialsAndInvokeV2(any(), any());
            assertThrows(
                    CfnNotFoundException.class,
                    () -> Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient)
            );
        }

        @Test
        public void testFindsAuthorization() {
            EndpointAuthorization endpointAuthorization = EndpointAuthorization.builder()
                    .allowedVPCs(vpcIds)
                    .build();

            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                    .endpointAuthorizationList(endpointAuthorization)
                    .build();

            doReturn(response).when(proxyClient).injectCredentialsAndInvokeV2(any(), any());

            assertEquals(vpcIds, Translator.getExistingVpcIds(account, clusterIdentifier, proxyClient));
        }
    }
}
