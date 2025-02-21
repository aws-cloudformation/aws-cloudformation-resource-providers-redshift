package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
  static RedshiftClient getClient() {
    return RedshiftClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }

  public static SecretsManagerClient secretsManagerClient() {
    return SecretsManagerClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
