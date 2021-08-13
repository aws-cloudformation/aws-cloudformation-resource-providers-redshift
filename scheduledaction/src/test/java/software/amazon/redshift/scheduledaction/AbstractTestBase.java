package software.amazon.redshift.scheduledaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.DeleteScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyScheduledActionResponse;
import software.amazon.awssdk.services.redshift.model.PauseClusterMessage;
import software.amazon.awssdk.services.redshift.model.ScheduledAction;
import software.amazon.awssdk.services.redshift.model.ScheduledActionState;
import software.amazon.awssdk.services.redshift.model.ScheduledActionType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;
    protected static final String AWS_REGION = "us-east-1";
    private static final Gson GSON = new GsonBuilder().create();
    private static final String SCHEDULED_ACTION_NAME;
    private static final ScheduledActionType TARGET_ACTION;
    private static final String SCHEDULE;
    private static final String IAM_ROLE;
    private static final String SCHEDULED_ACTION_DESCRIPTION;
    private static final ScheduledActionState STATE;
    private static final List<String> NEXT_INVOCATIONS;
    private static final Instant START_TIME;
    private static final Instant END_TIME;
    private static final boolean ENABLE;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();

        SCHEDULED_ACTION_NAME = "DummyScheduledActionName";
        TARGET_ACTION = ScheduledActionType.builder()
                .pauseCluster(PauseClusterMessage.builder()
                        .clusterIdentifier("DummyClusterID")
                        .build())
                .build();
        SCHEDULE = "DummySchedule";
        IAM_ROLE = "DummyIAMRole";
        SCHEDULED_ACTION_DESCRIPTION = "DummyDescription";
        STATE = ScheduledActionState.ACTIVE;
        NEXT_INVOCATIONS = Collections.emptyList();
        START_TIME = Instant.parse("9999-01-01T00:00:00Z");
        END_TIME = Instant.parse("9999-12-31T00:00:00Z");
        ENABLE = true;
    }

    static ProxyClient<RedshiftClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final RedshiftClient sdkClient) {
        return new ProxyClient<RedshiftClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public RedshiftClient client() {
                return sdkClient;
            }
        };
    }

    public static ResourceModel getCreateRequestResourceModel() {
        return ResourceModel.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .targetAction(GSON.fromJson(GSON.toJson(TARGET_ACTION), software.amazon.redshift.scheduledaction.ScheduledActionType.class))
                .schedule(SCHEDULE)
                .iamRole(IAM_ROLE)
                .scheduledActionDescription(SCHEDULED_ACTION_DESCRIPTION)
                .startTime(START_TIME.toString())
                .endTime(END_TIME.toString())
                .enable(ENABLE)
                .build();
    }

    public static CreateScheduledActionResponse getCreateResponseSdk() {
        return CreateScheduledActionResponse.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .targetAction(TARGET_ACTION)
                .schedule(SCHEDULE)
                .iamRole(IAM_ROLE)
                .scheduledActionDescription(SCHEDULED_ACTION_DESCRIPTION)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .state(STATE)
                .build();
    }

    public static ResourceModel getCreateResponseResourceModel() {
        ResourceModel model = getCreateRequestResourceModel();
        model.setState(STATE.toString());
        model.setNextInvocations(NEXT_INVOCATIONS);
        model.setEnable(null);

        return model;
    }

    public static ResourceModel getReadRequestResourceModel() {
        return ResourceModel.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .build();
    }

    public static DescribeScheduledActionsResponse getReadResponseSdk() {
        return DescribeScheduledActionsResponse.builder()
                .scheduledActions(Collections.singletonList(ScheduledAction.builder()
                        .scheduledActionName(SCHEDULED_ACTION_NAME)
                        .targetAction(TARGET_ACTION)
                        .schedule(SCHEDULE)
                        .iamRole(IAM_ROLE)
                        .scheduledActionDescription(SCHEDULED_ACTION_DESCRIPTION)
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .state(STATE)
                        .build()))
                .build();
    }

    public static ResourceModel getReadResponseResourceModel() {
        return ResourceModel.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .targetAction(GSON.fromJson(GSON.toJson(TARGET_ACTION), software.amazon.redshift.scheduledaction.ScheduledActionType.class))
                .schedule(SCHEDULE)
                .iamRole(IAM_ROLE)
                .scheduledActionDescription(SCHEDULED_ACTION_DESCRIPTION)
                .state(STATE.toString())
                .nextInvocations(NEXT_INVOCATIONS)
                .startTime(START_TIME.toString())
                .endTime(END_TIME.toString())
                .build();
    }

    public static ResourceModel getUpdateRequestResourceModel() {
        return getCreateRequestResourceModel();
    }

    public static ModifyScheduledActionResponse getUpdateResponseSdk() {
        return ModifyScheduledActionResponse.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .targetAction(TARGET_ACTION)
                .schedule(SCHEDULE)
                .iamRole(IAM_ROLE)
                .scheduledActionDescription(SCHEDULED_ACTION_DESCRIPTION)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .state(STATE)
                .build();
    }

    public static ResourceModel getUpdateResponseResourceModel() {
        return getCreateResponseResourceModel();
    }

    public static ResourceModel getDeleteRequestResourceModel() {
        return ResourceModel.builder()
                .scheduledActionName(SCHEDULED_ACTION_NAME)
                .build();
    }

    public static DeleteScheduledActionResponse getDeleteResponseSdk() {
        return DeleteScheduledActionResponse.builder().build();
    }

    public static ResourceModel getListRequestResourceModel() {
        return ResourceModel.builder().build();
    }

    public static DescribeScheduledActionsResponse getListResponsesSdk() {
        return getReadResponseSdk();
    }

    public static List<ResourceModel> getListResponsesResourceModel() {
        return Collections.singletonList(getReadResponseResourceModel());
    }
}
