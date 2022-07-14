package software.amazon.redshift.eventsubscription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.DeleteEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsResponse;
import software.amazon.awssdk.services.redshift.model.EventSubscription;
import software.amazon.awssdk.services.redshift.model.ModifyEventSubscriptionResponse;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;
    protected static final String AWS_REGION = "us-east-1";
    private static final Gson GSON = new GsonBuilder().create();
    private static final String SUBSCRIPTION_NAME;
    private static final String SNS_TOPIC_ARN;
    private static final String SOURCE_TYPE;
    private static final List<String> SOURCE_IDS;
    private static final Set<String> EVENT_CATEGORIES;
    private static final String SEVERITY;
    private static final boolean ENABLED;
    private static final List<Tag> TAGS;
    private static final String CUSTOMER_AWS_ID;
    private static final String CUST_SUBSCRIPTION_ID;
    private static final String STATUS;
    private static final Instant SUBSCRIPTION_CREATION_TIME;
    private static final List<String> SOURCE_IDS_LIST;
    private static final Set<String> EVENT_CATEGORIES_LIST;
    private static final EventSubscription EVENT_SUBSCRIPTION;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();

        SUBSCRIPTION_NAME = "DummySubscriptionName";
        SNS_TOPIC_ARN = "DummySnsTopicArn";
        SOURCE_TYPE = "DummySourceType";
        SOURCE_IDS = new ArrayList<String>() {{
            add("DummySourceId1");
            add("DummySourceId2");
        }};
        EVENT_CATEGORIES = new HashSet<String>() {{
            add("DummyEventCategory1");
            add("DummyEventCategory2");
        }};
        SEVERITY = "DummySeverity";
        ENABLED = true;
        TAGS = new ArrayList<Tag>() {{
            add(Tag.builder().key("DummyTag1").build());
            add(Tag.builder().key("DummyTag2").build());
        }};

        CUSTOMER_AWS_ID = "DummyCustomerAwsId";
        CUST_SUBSCRIPTION_ID = SUBSCRIPTION_NAME;
        STATUS = "DummyStatus";
        SUBSCRIPTION_CREATION_TIME = Instant.parse("9999-01-01T00:00:00Z");
        SOURCE_IDS_LIST = SOURCE_IDS;
        EVENT_CATEGORIES_LIST = EVENT_CATEGORIES;

        EVENT_SUBSCRIPTION = EventSubscription.builder()
                .customerAwsId(CUSTOMER_AWS_ID)
                .custSubscriptionId(CUST_SUBSCRIPTION_ID)
                .snsTopicArn(SNS_TOPIC_ARN)
                .status(STATUS)
                .subscriptionCreationTime(SUBSCRIPTION_CREATION_TIME)
                .sourceType(SOURCE_TYPE)
                .sourceIdsList(SOURCE_IDS_LIST)
                .eventCategoriesList(EVENT_CATEGORIES_LIST)
                .severity(SEVERITY)
                .enabled(ENABLED)
                .tags(TAGS)
                .build();
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
                .subscriptionName(SUBSCRIPTION_NAME)
                .snsTopicArn(SNS_TOPIC_ARN)
                .sourceType(SOURCE_TYPE)
                .sourceIds(SOURCE_IDS)
                .eventCategories(EVENT_CATEGORIES)
                .severity(SEVERITY)
                .enabled(ENABLED)
                .tags(translateToModelTags())
                .build();
    }

    public static CreateEventSubscriptionResponse getCreateResponseSdk() {
        return CreateEventSubscriptionResponse.builder()
                .eventSubscription(EVENT_SUBSCRIPTION)
                .build();
    }

    public static ResourceModel getCreateResponseResourceModel() {
        return ResourceModel.builder()
                .subscriptionName(SUBSCRIPTION_NAME)
                .snsTopicArn(SNS_TOPIC_ARN)
                .sourceType(SOURCE_TYPE)
                .sourceIds(SOURCE_IDS)
                .eventCategories(EVENT_CATEGORIES)
                .severity(SEVERITY)
                .enabled(ENABLED)
                .tags(translateToModelTags())
                .customerAwsId(CUSTOMER_AWS_ID)
                .custSubscriptionId(CUST_SUBSCRIPTION_ID)
                .status(STATUS)
                .subscriptionCreationTime(SUBSCRIPTION_CREATION_TIME.toString())
                .sourceIdsList(SOURCE_IDS_LIST)
                .eventCategoriesList(EVENT_CATEGORIES_LIST)
                .build();
    }

    public static ResourceModel getReadRequestResourceModel() {
        return ResourceModel.builder()
                .subscriptionName(SUBSCRIPTION_NAME)
                .build();
    }

    public static DescribeEventSubscriptionsResponse getReadResponseSdk() {
        return DescribeEventSubscriptionsResponse.builder()
                .eventSubscriptionsList(Collections.singletonList(EVENT_SUBSCRIPTION))
                .build();
    }

    public static ResourceModel getReadResponseResourceModel() {
        return getCreateResponseResourceModel();
    }

    public static ResourceModel getUpdateRequestResourceModel() {
        return getCreateRequestResourceModel();
    }

    public static ModifyEventSubscriptionResponse getUpdateResponseSdk() {
        return ModifyEventSubscriptionResponse.builder()
                .eventSubscription(EVENT_SUBSCRIPTION)
                .build();
    }

    public static ResourceModel getUpdateResponseResourceModel() {
        return getCreateResponseResourceModel();
    }

    public static ResourceModel getDeleteRequestResourceModel() {
        return ResourceModel.builder()
                .subscriptionName(SUBSCRIPTION_NAME)
                .build();
    }

    public static DeleteEventSubscriptionResponse getDeleteResponseSdk() {
        return DeleteEventSubscriptionResponse.builder().build();
    }

    public static ResourceModel getListRequestResourceModel() {
        return ResourceModel.builder().build();
    }

    public static DescribeEventSubscriptionsResponse getListResponsesSdk() {
        return getReadResponseSdk();
    }

    public static List<ResourceModel> getListResponsesResourceModel() {
        return Collections.singletonList(ResourceModel.builder()
                .subscriptionName(SUBSCRIPTION_NAME)
                .build());
    }

    private static List<software.amazon.redshift.eventsubscription.Tag> translateToModelTags() {
        return AbstractTestBase.TAGS
                .stream()
                .map(tag -> GSON.fromJson(GSON.toJson(tag), software.amazon.redshift.eventsubscription.Tag.class))
                .collect(Collectors.toList());
    }
}
