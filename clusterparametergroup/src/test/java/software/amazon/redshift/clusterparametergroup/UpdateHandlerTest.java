package software.amazon.redshift.clusterparametergroup;

import java.util.Arrays;
import java.util.List;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroup;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static software.amazon.redshift.clusterparametergroup.TestUtils.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new UpdateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        final DescribeClusterParameterGroupsResponse describeClusterParameterGroupsResponse = DescribeClusterParameterGroupsResponse.builder()
                .parameterGroups(ClusterParameterGroup.builder().build())
                .build();

        doReturn(ModifyClusterParameterGroupResponse.builder().build(),
                DESCRIBE_TAGS_RESPONSE,
                describeClusterParameterGroupsResponse,
                DESCRIBE_CLUSTER_PARAMETERS_RESPONSE)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_RemoveParameters() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .build();

        final DescribeClusterParameterGroupsResponse describeClusterParameterGroupsResponse = DescribeClusterParameterGroupsResponse.builder()
                .parameterGroups(ClusterParameterGroup.builder().build())
                .build();
        final DescribeClusterParametersResponse describeClusterParametersResponse = DescribeClusterParametersResponse.builder().build();

        doReturn(ModifyClusterParameterGroupResponse.builder().build(),
                DESCRIBE_TAGS_RESPONSE,
                describeClusterParameterGroupsResponse,
                describeClusterParametersResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Tagging() {
        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        final List<TaggedResource> taggedResources = Arrays.asList(
                TaggedResource.builder().tag(Tag.builder().key("key1").value("val1").build()).build(),
                TaggedResource.builder().tag(Tag.builder().key("key4").value("val4").build()).build()
        );
        final DescribeTagsResponse describeTagsResponse = DescribeTagsResponse.builder()
                .taggedResources(taggedResources)
                .build();

        doReturn(ModifyClusterParameterGroupResponse.builder().build(),
                describeTagsResponse,
                DESCRIBE_CLUSTER_PARAMETER_GROUPS_RESPONSE,
                DescribeClusterParametersResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidClusterParameterGroupStateException() {
        doThrow(InvalidClusterParameterGroupStateException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ClusterParameterGroupNotFoundException() {
        doThrow(ClusterParameterGroupNotFoundException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_InvalidTagException() {
        doReturn(ResetClusterParameterGroupResponse.builder().build(),
                DESCRIBE_TAGS_RESPONSE,
                DESCRIBE_CLUSTER_PARAMETER_GROUPS_RESPONSE,
                DescribeClusterParametersResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        doThrow(InvalidTagException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(CreateTagsRequest.class), any());

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION_GOV)
                .build();

        assertThrows(CfnGeneralServiceException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ResourceNotFoundException() {
        doReturn(ResetClusterParameterGroupResponse.builder().build(),
                DESCRIBE_TAGS_RESPONSE,
                DESCRIBE_CLUSTER_PARAMETER_GROUPS_RESPONSE,
                DescribeClusterParametersResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        doThrow(ResourceNotFoundException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(CreateTagsRequest.class), any());

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION_CN)
                .build();

        assertThrows(CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}