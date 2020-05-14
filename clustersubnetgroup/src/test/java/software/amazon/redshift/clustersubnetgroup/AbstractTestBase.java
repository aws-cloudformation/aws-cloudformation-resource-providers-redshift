package software.amazon.redshift.clustersubnetgroup;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import org.slf4j.LoggerFactory;


public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;
  protected static final org.slf4j.Logger delegate;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");

    delegate = LoggerFactory.getLogger("testing");
    logger = new LoggerProxy();
  }

  static ProxyClient<RedshiftClient> MOCK_PROXY(final AmazonWebServicesClientProxy proxy,
                                                final RedshiftClient sdkClient){

    return new ProxyClient<RedshiftClient>() {

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
      ResponseT
      injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction){
        return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> CompletableFuture<ResponseT> injectCredentialsAndInvokeV2Async(RequestT requestT, Function<RequestT, CompletableFuture<ResponseT>> function){
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>> IterableT injectCredentialsAndInvokeIterableV2(RequestT requestT, Function<RequestT, IterableT> function){
        return null;
      }

      @Override
      public RedshiftClient client(){
        return sdkClient;
      }
    };
  }
}
