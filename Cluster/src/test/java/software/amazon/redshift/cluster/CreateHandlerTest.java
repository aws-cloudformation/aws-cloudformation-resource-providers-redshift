package software.amazon.redshift.cluster;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.cluster.TestUtils.AWS_REGION;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER_READ;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.BUCKET_NAME;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERPASSWORD;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class)))
                .thenReturn(CreateClusterResponse.builder()
                        .cluster(BASIC_CLUSTER)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER_READ)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setMasterUserPassword(MASTER_USERPASSWORD);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());

        Assert.assertEquals("expected not equal to actual",response.getResourceModel(), request.getDesiredResourceState());

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));

    }

    @Test
    public void testCreateClusterLoggingAndTags() {
        ResourceModel model = BASIC_MODEL;
        LoggingProperties loggingProperties = LoggingProperties.builder()
                .bucketName(BUCKET_NAME)
                .build();

        Tag tag = Tag.builder()
                .key("foo")
                .value("bar")
                .build();

        List<Tag> tags = new LinkedList<>();
        tags.add(tag);

        model.setLoggingProperties(loggingProperties);
        model.setTags(tags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class)))
                .thenReturn(CreateClusterResponse.builder()
                        .cluster(BASIC_CLUSTER)
                        .build());

        when(proxyClient.client().enableLogging(any(EnableLoggingRequest.class)))
                .thenReturn(EnableLoggingResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER_READ)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .build());

//        software.amazon.awssdk.services.redshift.model.Tag clusterTag =
//                software.amazon.awssdk.services.redshift.model.Tag.builder()
//                        .key("foo")
//                        .value("bar")
//                        .build();
//
//        TaggedResource taggedResource = TaggedResource.builder()
//                .tag(clusterTag)
//                .build();
//
//        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
//                .thenReturn(DescribeTagsResponse.builder()
//                        .taggedResources(taggedResource)
//                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setMasterUserPassword(MASTER_USERPASSWORD);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());

        Assert.assertEquals("expected not equal to actual",response.getResourceModel(), request.getDesiredResourceState());

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(4))
                .describeClusters(any(DescribeClustersRequest.class));

    }
}
