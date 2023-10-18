package software.amazon.redshift.resourcepolicy;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.model.DeleteResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.ResourcePolicy;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;

  protected static final String RESOURCE_ARN;

  protected static final String POLICY;

  protected static final ResourcePolicy RESOURCE_POLICY;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
    RESOURCE_ARN = "DummyResourceArn";
    POLICY = "DummyResourcePolicy";

    RESOURCE_POLICY = ResourcePolicy.builder()
            .resourceArn(RESOURCE_ARN)
            .policy(POLICY)
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

  public static ResourceModel putResourcePolicyRequestResourceModel() {
    return ResourceModel.builder()
            .resourceArn(RESOURCE_ARN)
            .policy(POLICY)
            .build();
  }

  public static PutResourcePolicyResponse putResourcePolicyResponseSdk() {
    return PutResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY)
            .build();
  }

  public static ResourceModel getResourcePolicyRequestResourceModel() {
    return ResourceModel.builder()
            .resourceArn(RESOURCE_ARN)
            .build();
  }

  public static ResourceModel getResourcePolicyResponseResourceModel() {
    return putResourcePolicyRequestResourceModel();
  }

  public static GetResourcePolicyResponse getResourcePolicyResponseSdk() {
    return GetResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY)
            .build();
  }

  public static ResourceModel deleteResourcePolicyRequestResourceModel() {
    return ResourceModel.builder()
            .resourceArn(RESOURCE_ARN)
            .build();
  }

  public static DeleteResourcePolicyResponse deleteResourcePolicyResponseSdk() {
    return DeleteResourcePolicyResponse.builder().build();
  }
}
