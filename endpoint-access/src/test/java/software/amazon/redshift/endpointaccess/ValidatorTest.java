package software.amazon.redshift.endpointaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAccess;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class ValidatorTest {
    LoggerProxy logger = new LoggerProxy();
    @Mock ResourceModel resourceModel;
    @Mock ProxyClient<RedshiftClient> proxyClient;
    @Mock RedshiftClient redshiftClient;

    String endpointName = "endpoint-name";
    String subnetGroupName = "subnet-group-name";

    public static abstract class ValidatorAbstractTest extends AbstractStaticTest<Validator> {
        Class<Validator> getMockedClass() {
            return Validator.class;
        }
    }

    @Nested
    @DisplayName("SatisfiesEndpointConstraints")
    public class SatisfiesEndpointConstraintsTest extends ValidatorAbstractTest {
        String validEndpointName = "valid-endpoint-name";

        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.satisfiesEndpointConstraints(any());
        }

        @Test
        public void testEndpointRegexMatch() {
            assertTrue(Validator.satisfiesEndpointConstraints(validEndpointName));
        }

        @Test
        public void testEndpointRegexNoMatch() {
            String validEndpointName = "ßad-endpoint-name!悪いエンドポイント！";
            assertFalse(Validator.satisfiesEndpointConstraints(validEndpointName));
        }

        @Test
        public void testEndpointLengthInvalid() {
            String invalidLengthEndpointName = "this-endpoint-matches-regex-but-is-above-30-char-limit";
            assertFalse(Validator.satisfiesEndpointConstraints(invalidLengthEndpointName));
        }
    }

    @Nested
    @DisplayName("ValidateCreateRequest")
    public class ValidateCreateRequestTest extends ValidatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.validateCreateRequest(any(), any());
        }

        @Test
        public void testValidateCreateRequest() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            doReturn(subnetGroupName).when(resourceModel).getSubnetGroupName();

            mockedClass.when(() -> Validator.isEmpty(any())).thenReturn(false);
            mockedClass.when(() -> Validator.satisfiesEndpointConstraints(endpointName)).thenReturn(true);

            Validator.validateCreateRequest(resourceModel, logger);
        }

        @Test
        public void testValidateEmptyEndpointName() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(true);

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateCreateRequest(resourceModel, logger)
            );
        }

        @Test
        public void testValidateEmptySubnetGroupName() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            doReturn(subnetGroupName).when(resourceModel).getSubnetGroupName();

            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(false);
            mockedClass.when(() -> Validator.isEmpty(subnetGroupName)).thenReturn(true);

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateCreateRequest(resourceModel, logger)
            );
        }

        @Test
        public void testValidateBadEndpointName() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            doReturn(subnetGroupName).when(resourceModel).getSubnetGroupName();

            mockedClass.when(() -> Validator.isEmpty(any())).thenReturn(false);
            mockedClass.when(() -> Validator.satisfiesEndpointConstraints(endpointName)).thenReturn(false);

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateCreateRequest(resourceModel, logger)
            );
        }

        @Test
        public void testNullChecks() {
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateCreateRequest(null, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateCreateRequest(resourceModel, null)
            );
        }
    }

    @Nested
    @DisplayName("ValidateDeleteRequest")
    public class ValidateDeleteRequestTest extends ValidatorAbstractTest{
        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.validateDeleteRequest(any(), any());
        }

        @Test
        public void testValidateDeleteRequest() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(false);

            Validator.validateDeleteRequest(resourceModel, logger);
            mockedClass.verify(
                    atLeastOnce(),
                    () -> Validator.logFieldsCheck(any(), eq(resourceModel), eq("DELETE"), eq(logger))
            );
        }

        @Test
        public void testValidateDeleteRequestInvalid() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(true);

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateDeleteRequest(resourceModel, logger)
            );
        }

        @Test
        public void testNullChecks() {
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateDeleteRequest(null, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateDeleteRequest(resourceModel, null)
            );
        }
    }

    @Nested
    @DisplayName("ValidateReadRequest")
    public class ValidateReadRequestTest extends ValidatorAbstractTest {
        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.validateReadRequest(any(), any());
        }

        @Test
        public void testValidateReadRequest() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(false);

            Validator.validateReadRequest(resourceModel, logger);
            mockedClass.verify(
                    atLeastOnce(),
                    () -> Validator.logFieldsCheck(any(), eq(resourceModel), eq("READ"), eq(logger))
            );
        }

        @Test
        public void testValidateReadRequestInvalid() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(true);

            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateReadRequest(resourceModel, logger)
            );
        }

        @Test
        public void testNullChecks() {
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateReadRequest(null, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateReadRequest(resourceModel, null)
            );
        }
    }

    @Nested
    @DisplayName("ValidateCreateOnlyProperty")
    public class ValidateCreateOnlyPropertyTest extends ValidatorAbstractTest {
        String resourceValue = "value";
        String matchingEndpointAccessValue = "value";
        String unmatchedValue = "this does not match";

        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.createOnlyPropertyMatches(any(), any(), any());
        }

        @Test
        public void testValidateNullCreateOnlyProperty() {
            assertTrue(Validator.createOnlyPropertyMatches(resourceValue, null, logger));
        }

        @Test
        public void testValidateCreateOnlyProperty() {
            assertTrue(Validator.createOnlyPropertyMatches(resourceValue, matchingEndpointAccessValue, logger));
        }

        @Test
        public void testValidateNotMatchingCreateOnlyProperties() {
            assertFalse(Validator.createOnlyPropertyMatches(resourceValue, unmatchedValue, logger));
        }

        @Test
        public void testNullChecks() {
            EndpointAccess endpointAccess = EndpointAccess.builder().build();
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.createOnlyPropertyMatches(null, resourceModel, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.createOnlyPropertyMatches(endpointAccess, resourceModel, null)
            );
        }
    }

    @Nested
    @DisplayName("ValidateCreateOnlyProperties")
    public class ValidateCreateOnlyPropertiesTest extends ValidatorAbstractTest {
        @Mock EndpointAccess endpointAccess;
        @Mock HashMap<String, String> propertyValuePairs;

        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.validateCreateOnlyProperties(any(), any(), any());
        }

        @Test
        public void testValidateCreateOnlyProperties() {
            mockedClass.when(() -> Validator.buildPropertyValuePairs(endpointAccess, resourceModel))
                    .thenReturn(propertyValuePairs);
            mockedClass.when(() -> Validator.createOnlyPropertiesMatch(propertyValuePairs, logger))
                    .thenReturn(true);

            Validator.validateCreateOnlyProperties(endpointAccess, resourceModel, logger);
        }

        @Test
        public void testValidateCreateOnlyPropertiesDoNotMatch() {
            mockedClass.when(() -> Validator.buildPropertyValuePairs(endpointAccess, resourceModel))
                    .thenReturn(propertyValuePairs);
            mockedClass.when(() -> Validator.createOnlyPropertiesMatch(propertyValuePairs, logger))
                    .thenReturn(false);

            assertThrows(
                    CfnNotUpdatableException.class,
                    () -> Validator.validateCreateOnlyProperties(endpointAccess, resourceModel, logger)
            );
        }

        @Test
        public void testNullChecks() {
            EndpointAccess endpointAccess = EndpointAccess.builder().build();
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateCreateOnlyProperties(null, resourceModel, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateCreateOnlyProperties(endpointAccess, null, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateCreateOnlyProperties(endpointAccess, resourceModel, null)
            );
        }
    }

    @Nested
    @DisplayName("CreateOnlyPropertiesMatch")
    public class CreateOnlyPropertiesMatchTest extends ValidatorAbstractTest {
        String key = "key";
        String value = "value";
        @Mock HashMap<String, String> exampleMap;
        @Mock Map.Entry<String, String> exampleEntry;
        Set<Map.Entry<String, String>> exampleEntrySet;

        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.createOnlyPropertiesMatch(any(), any());
        }

        @BeforeEach
        void setup() {
            exampleEntrySet = new HashSet<Map.Entry<String, String>>(){{
                add(exampleEntry);
            }};

            doReturn(exampleEntrySet).when(exampleMap).entrySet();

            doReturn(key).when(exampleEntry).getKey();
            doReturn(value).when(exampleEntry).getValue();
        }

        @Test
        public void testPropertiesMatch() {
            mockedClass.when(() -> Validator.createOnlyPropertyMatches(key, value, logger)).thenReturn(true);
            assertTrue(Validator.createOnlyPropertiesMatch(exampleMap, logger));
        }

        @Test
        public void testPropertiesDoNotMatch() {
            mockedClass.when(() -> Validator.createOnlyPropertyMatches(key, value, logger)).thenReturn(false);
            assertFalse(Validator.createOnlyPropertiesMatch(exampleMap, logger));
        }

    }

    @Nested
    @DisplayName("ValidateUpdateRequest")
    public class ValidateUpdateRequestTest extends ValidatorAbstractTest {
        @Mock EndpointAccess endpointAccess;
        @Mock List<EndpointAccess> endpointAccessList;
        @Mock DescribeEndpointAccessResponse response;

        MockedStatic.Verification getVerificationFunction() {
            return () -> Validator.validateUpdateRequest(any(), any(), any());
        }

        @Test
        public void testNullChecks() {
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateUpdateRequest(null, resourceModel, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateUpdateRequest(proxyClient, null, logger)
            );
            assertThrows(
                    NullPointerException.class,
                    () -> Validator.validateUpdateRequest(proxyClient, resourceModel, null)
            );
        }

        @Test
        public void testValidateUpdateRequest() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(false);
            doReturn(response).when(proxyClient).injectCredentialsAndInvokeV2(any(), any());
            doReturn(redshiftClient).when(proxyClient).client();
            doReturn(endpointAccess).when(endpointAccessList).get(0);
            doReturn(endpointAccessList).when(response).endpointAccessList();
            Validator.validateUpdateRequest(proxyClient, resourceModel, logger);
            mockedClass.verify(
                    atLeastOnce(),
                    () -> Validator.logFieldsCheck(any(), eq(resourceModel), eq("UPDATE"), eq(logger))
            );
        }

        @Test
        public void testEmptyEndpointName() {
            doReturn(endpointName).when(resourceModel).getEndpointName();

            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(true);
            assertThrows(
                    CfnInvalidRequestException.class,
                    () -> Validator.validateUpdateRequest(proxyClient, resourceModel, logger)
            );
        }

        @Test
        public void testThrowsCfnNotFoundException() {
            doReturn(endpointName).when(resourceModel).getEndpointName();
            mockedClass.when(() -> Validator.isEmpty(endpointName)).thenReturn(false);
            doReturn(redshiftClient).when(proxyClient).client();
            doThrow(EndpointNotFoundException.class).when(proxyClient).injectCredentialsAndInvokeV2(any(), any());
            assertThrows(
                    CfnNotFoundException.class,
                    () -> Validator.validateUpdateRequest(proxyClient, resourceModel, logger)
            );
        }
    }

    @Test
    public void testBuildPropertyValuePairs() {
        String resourceOwner = "resourceOwner";
        String clusterIdentifier = "clusterIdentifier";

        EndpointAccess endpointAccess = EndpointAccess.builder()
                .endpointName(endpointName)
                .subnetGroupName(subnetGroupName)
                .resourceOwner(resourceOwner)
                .clusterIdentifier(clusterIdentifier)
                .build();

        ResourceModel realResourceModel = ResourceModel.builder()
                .endpointName(endpointName)
                .subnetGroupName(subnetGroupName)
                .resourceOwner(resourceOwner)
                .clusterIdentifier(clusterIdentifier)
                .build();

        HashMap<String, String> expectedValue = new HashMap<String, String>() {{
            put(endpointAccess.endpointName(),  realResourceModel.getEndpointName());
            put(endpointAccess.subnetGroupName(),  realResourceModel.getSubnetGroupName());
            put(endpointAccess.resourceOwner(),  realResourceModel.getResourceOwner());
            put(endpointAccess.clusterIdentifier(),  realResourceModel.getClusterIdentifier());
        }};

        assertEquals(expectedValue, Validator.buildPropertyValuePairs(endpointAccess, realResourceModel));
    }

    @Test
    public void testIsEmpty() {
        try (MockedStatic<Validator> mockedClass = Mockito.mockStatic(Validator.class)) {
            mockedClass.when(() -> Validator.isEmpty(any())).thenCallRealMethod();

            assertTrue(Validator.isEmpty(""));
            assertTrue(Validator.isEmpty(null));
            assertFalse(Validator.isEmpty("not empty!"));
        }
    }
}
