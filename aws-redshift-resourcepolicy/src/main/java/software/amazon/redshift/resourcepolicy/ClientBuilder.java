package software.amazon.redshift.resourcepolicy;

import java.net.URI;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {

  public static RedshiftClient getClient() {
    return RedshiftClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .endpointOverride(URI.create("https://aws-cookie-monster-devo.amazon.com"))
            .build();
  }
}
