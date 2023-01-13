package software.amazon.redshift.cluster;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.LambdaWrapper;
public class ClientBuilder {
  static RedshiftClient getClient() {
    return RedshiftClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
