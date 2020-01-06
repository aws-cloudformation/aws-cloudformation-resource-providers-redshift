package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.LambdaWrapper;

final class ClientBuilder {
    static RedshiftClient getClient() {
        return RedshiftClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}