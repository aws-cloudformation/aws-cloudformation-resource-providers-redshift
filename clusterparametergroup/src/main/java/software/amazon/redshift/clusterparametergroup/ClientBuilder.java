package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.LambdaWrapper;
// TODO: replace all usage of SdkClient with your service client type, e.g; YourServiceClient
// import software.amazon.awssdk.services.yourservice.YourServiceClient;
// import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
  public static RedshiftClient getClient() {
    return RedshiftClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
