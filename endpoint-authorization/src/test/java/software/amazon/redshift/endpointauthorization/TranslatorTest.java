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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    List<String> vpcIds = Collections.singletonList(vpcId);
    boolean force = RandomUtils.nextBoolean();

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
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
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
                .vpcIds(vpcIds)
                .allowedAllVPCs(vpcIds.isEmpty())
                .build();

        @Test
        public void testNullVpcIdsToAdd() {
            AuthorizeEndpointAccessRequest expectedRequest = AuthorizeEndpointAccessRequest.builder().build();

            assertEquals(expectedRequest, Translator.translateToUpdateAuthorizeRequest(resourceModel, resourceModel));
        }

        @Test
        public void testFoundVpcIdsToAdd() {
            List<String> desiredVpcIds = Collections.singletonList(vpcId + "2");
            AuthorizeEndpointAccessRequest expectedRequest = AuthorizeEndpointAccessRequest.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .vpcIds(desiredVpcIds)
                    .build();

            ResourceModel desiredResourceModel = ResourceModel.builder()
                    .account(account)
                    .clusterIdentifier(clusterIdentifier)
                    .vpcIds(desiredVpcIds)
                    .allowedAllVPCs(false)
                    .build();

            assertEquals(expectedRequest, Translator.translateToUpdateAuthorizeRequest(desiredResourceModel, resourceModel));
        }
    }

    @Nested
    @DisplayName("TranslateToUpdateRevokeRequest")
    public class TranslateToUpdateRevokeRequestTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToUpdateRevokeRequest(any(), any(), anyBoolean());
        }

        ResourceModel resourceModel = ResourceModel.builder()
                .clusterIdentifier(clusterIdentifier)
                .account(account)
                .vpcIds(vpcIds)
                .allowedAllVPCs(vpcIds.isEmpty())
                .build();

        @Test
        public void testNullVpcIdsToRemove() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder().build();

            assertEquals(expectedRequest, Translator.translateToUpdateRevokeRequest(resourceModel, resourceModel, force));
        }

        @Test
        public void testFoundVpcIdsToRemove() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .vpcIds(vpcIds)
                    .force(force)
                    .build();

            ResourceModel desiredResourceModel = ResourceModel.builder()
                    .account(account)
                    .clusterIdentifier(clusterIdentifier)
                    .vpcIds(Collections.emptyList())
                    .build();

            assertEquals(expectedRequest, Translator.translateToUpdateRevokeRequest(desiredResourceModel, resourceModel, force));
        }
    }

    @Nested
    @DisplayName("GetVpcIdsToAdd")
    public class GetVpcIdsToAddTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToUpdateAuthorizeRequest(any(), any());
        }

        @Test
        public void testNoneToAll() {
            List<String> expectedVpcIds = Collections.singletonList("vpc-1");

            ResourceModel oldResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.emptyList())
                    .allowedAllVPCs(true)
                    .build();

            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .allowedAllVPCs(false)
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateAuthorizeRequest(newResourceModel, oldResourceModel).vpcIds());
        }

        @Test
        public void testAllToNone() {
            List<String> expectedVpcIds = Collections.emptyList();

            ResourceModel oldResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .allowedAllVPCs(false)
                    .build();

            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.emptyList())
                    .allowedAllVPCs(true)
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateAuthorizeRequest(newResourceModel, oldResourceModel).vpcIds());
        }

        @Test
        public void testDownsize() {
            List<String> expectedVpcIds = Collections.emptyList();

            ResourceModel oldResourceModel = ResourceModel.builder()
                    .vpcIds(Arrays.asList("vpc-1", "vpc-2"))
                    .allowedAllVPCs(false)
                    .build();

            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .allowedAllVPCs(false)
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateAuthorizeRequest(newResourceModel, oldResourceModel).vpcIds());
        }

        @Test
        public void testUpsize() {
            List<String> expectedVpcIds = Collections.singletonList("vpc-2");

            ResourceModel oldResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .allowedAllVPCs(false)
                    .build();

            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Arrays.asList("vpc-1", "vpc-2"))
                    .allowedAllVPCs(false)
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateAuthorizeRequest(newResourceModel, oldResourceModel).vpcIds());
        }
    }

    @Nested
    @DisplayName("GetVpcIdsToRemove")
    public class GetVpcIdsToRemoveTest extends TranslatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Translator.translateToUpdateRevokeRequest(any(), any(), anyBoolean());
        }
        @Mock ResourceModel resourceModel;

        @Test
        public void testNoneToAll() {
            List<String> expectedVpcIds = Collections.emptyList();

            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateRevokeRequest(newResourceModel, resourceModel, force).vpcIds());
        }

        @Test
        public void testAllToNone() {
            List<String> expectedVpcIds = Collections.singletonList("vpc-1");

            doReturn(Collections.singletonList("vpc-1")).when(resourceModel).getVpcIds();
            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.emptyList())
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateRevokeRequest(newResourceModel, resourceModel, force).vpcIds());
        }

        @Test
        public void testDownsize() {
            List<String> expectedVpcIds = Collections.singletonList("vpc-2");

            doReturn(Arrays.asList("vpc-1", "vpc-2")).when(resourceModel).getVpcIds();
            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Collections.singletonList("vpc-1"))
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateRevokeRequest(newResourceModel, resourceModel, force).vpcIds());
        }

        @Test
        public void testUpsize() {
            List<String> expectedVpcIds = Collections.emptyList();

            doReturn(Collections.singletonList("vpc-1")).when(resourceModel).getVpcIds();
            ResourceModel newResourceModel = ResourceModel.builder()
                    .vpcIds(Arrays.asList("vpc-1", "vpc-2"))
                    .build();

            assertEquals(expectedVpcIds, Translator.translateToUpdateRevokeRequest(newResourceModel, resourceModel, force).vpcIds());
        }
    }

    @Nested
    @DisplayName("TranslateFromReadResponse")
    public class TranslateFromReadResponseTest {
        @Test
        public void testTranslateEmptyResponse() {
            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                    .endpointAuthorizationList(new ArrayList<>())
                    .build();

            assertEquals(ResourceModel.builder().build(), Translator.translateFromReadResponse(response));
        }
        @Test
        public void testTranslate() {
            String grantor = "grantor";
            String clusterStatus = "cluster status";
            Instant authorizeTime = Instant.now();
            AuthorizationStatus authorizationStatus = AuthorizationStatus.AUTHORIZED;
            Boolean allowedAllVPCs = RandomUtils.nextBoolean();
            Integer endpointCount = 1;

            EndpointAuthorization endpointAuthorization = EndpointAuthorization.builder()
                    .grantor(grantor)
                    .grantee(account)
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
                    .grantee(account)
                    .clusterIdentifier(clusterIdentifier)
                    .authorizeTime(authorizeTime.toString())
                    .clusterStatus(clusterStatus)
                    .status(authorizationStatus.toString())
                    .allowedAllVPCs(allowedAllVPCs)
                    .allowedVPCs(vpcIds)
                    .endpointCount(endpointCount)
                    .account(account)
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
                    .force(force)
                    .clusterIdentifier(clusterIdentifier)
                    .vpcIds(vpcIds)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .account(account)
                    .force(force)
                    .clusterIdentifier(clusterIdentifier)
                    .vpcIds(vpcIds)
                    .build();

            assertEquals(expectedRequest, Translator.translateToRevokeRequest(model));
        }

        @Test
        public void testTranslateNoVpcIds() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .account(account)
                    .force(force)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .account(account)
                    .force(force)
                    .clusterIdentifier(clusterIdentifier)
                    .build();

            assertEquals(expectedRequest, Translator.translateToRevokeRequest(model));
        }

        @Test
        public void testTranslateNoAccount() {
            RevokeEndpointAccessRequest expectedRequest = RevokeEndpointAccessRequest.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .force(force)
                    .build();

            ResourceModel model = ResourceModel.builder()
                    .clusterIdentifier(clusterIdentifier)
                    .account(account)
                    .force(force)
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

        List<ResourceModel> expectedList = Collections.singletonList(
                ResourceModel.builder()
                        .clusterIdentifier(clusterIdentifier)
                        .account(account)
                        .build()
        );

        DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder()
                .endpointAuthorizationList(Collections.singletonList(endpointAuthorization))
                .build();

        assertEquals(expectedList, Translator.translateFromListRequest(response));
    }
}
