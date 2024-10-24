package software.amazon.redshift.integration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
  public static RedshiftClient getClient() {
    return RedshiftClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
