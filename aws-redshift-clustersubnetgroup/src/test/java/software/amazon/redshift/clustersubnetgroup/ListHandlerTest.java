package software.amazon.redshift.clustersubnetgroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_CLUSTER_SUBNET_GROUP;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_MODEL;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        System.setProperty("aws.region", AWS_REGION);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(BASIC_MODEL)
                .region(AWS_REGION)
                .build();

        doReturn(DescribeClusterSubnetGroupsResponse.builder()
                .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                .marker("")
                .build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_WithPagination() {
        final ListHandler handler = new ListHandler();
        final String nextToken = "next-token";

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(BASIC_MODEL)
                .region(AWS_REGION)
                .nextToken("current-token")
                .build();

        doReturn(DescribeClusterSubnetGroupsResponse.builder()
                .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                .marker(nextToken)
                .build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getNextToken()).isEqualTo(nextToken);
        assertThat(response.getResourceModels()).isNotNull();
    }

    @Test
    public void handleRequest_EmptyList() {
        final ListHandler handler = new ListHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .build();

        doReturn(DescribeClusterSubnetGroupsResponse.builder()
                .clusterSubnetGroups(java.util.Collections.emptyList())
                .build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).isEmpty();
    }
}