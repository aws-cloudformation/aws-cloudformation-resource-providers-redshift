package software.amazon.redshift.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.redshift.model.Integration;
import software.amazon.awssdk.services.redshift.model.ZeroETLIntegrationStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.delay.Constant;

public class AbstractTestBase {

  protected static final String STACK_ID = "stackid";
  protected static final String LOGICAL_RESOURCE_IDENTIFIER = "integrationresource";

  protected static final Credentials MOCK_CREDENTIALS;
  protected static final org.slf4j.Logger delegate;
  protected static final LoggerProxy logger;

  static final Set<Tag> TAG_LIST;
  static final Set<Tag> TAG_LIST_EMPTY;
  static final Set<Tag> TAG_LIST_ALTER;
  static final Tagging.TagSet TAG_SET;

  // use an accelerated backoff for faster unit testing
  protected final HandlerConfig TEST_HANDLER_CONFIG = HandlerConfig.builder()
          .probingEnabled(false)
          .backoff(Constant.of().delay(Duration.ofMillis(1))
                  .timeout(Duration.ofSeconds(120))
                  .build())
          .build();

  static {
    System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
    System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss:SSS Z");
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");

    delegate = LoggerFactory.getLogger("testing");
    logger = new LoggerProxy();

    TAG_LIST_EMPTY = ImmutableSet.of();

    TAG_LIST = ImmutableSet.of(
            Tag.builder().key("k1").value("kv1").build(),
            Tag.builder().key("k4").value("kv4").build()
    );

    TAG_LIST_ALTER = ImmutableSet.of(
            Tag.builder().key("k1").value("kv2").build(),
            Tag.builder().key("k2").value("kv2").build(),
            Tag.builder().key("k3").value("kv3").build()
    );

    TAG_SET = Tagging.TagSet.builder()
            .systemTags(ImmutableSet.of(
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("system-tag-1").value("system-tag-value1").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("system-tag-2").value("system-tag-value2").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("system-tag-3").value("system-tag-value3").build()
            )).stackTags(ImmutableSet.of(
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("stack-tag-1").value("stack-tag-value1").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("stack-tag-2").value("stack-tag-value2").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("stack-tag-3").value("stack-tag-value3").build()
            )).resourceTags(ImmutableSet.of(
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("resource-tag-1").value("resource-tag-value1").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("resource-tag-2").value("resource-tag-value2").build(),
                    software.amazon.awssdk.services.redshift.model.Tag.builder().key("resource-tag-3").value("resource-tag-value3").build()
            )).build();
  }

  protected static final String INTEGRATION_NAME = "integration-identifier-1";
  protected static final String INTEGRATION_ARN = "arn:aws:redshift:us-east-1:123456789012:integration:de4b78a2-0bff-4e93-814a-bacd3f81b383";
  protected static final String SOURCE_ARN = "arn:aws:dynamo:us-east-1:123456789012:cluster:cfn-integ-test-prov-5-rdsdbcluster-ozajchztpipc";
  protected static final String TARGET_ARN = "arn:aws:redshift:us-east-1:123456789012:namespace:ad99c581-dbac-4a1b-9602-d5c5e7f77b24";
  protected static final String KMS_KEY_ID = "arn:aws:kms:us-east-1:123456789012:key/9d67ba2d-daca-4e3c-ac23-16342062ede3";
  protected static final Map<String, String> ADDITIONAL_ENCRYPTION_CONTEXT = ImmutableMap.of("eck1", "ecv1", "eck2", "ecv2");

  static final Integration INTEGRATION_ACTIVE = Integration.builder()
          .integrationArn(INTEGRATION_ARN)
          .integrationName(INTEGRATION_NAME)
          .sourceArn(SOURCE_ARN)
          .targetArn(TARGET_ARN)
          .kmsKeyId(KMS_KEY_ID)
          .status(ZeroETLIntegrationStatus.ACTIVE)
          .createTime(Instant.ofEpochMilli(1699489854712L))
          .additionalEncryptionContext(ADDITIONAL_ENCRYPTION_CONTEXT)
          .tags(toAPITags(TAG_LIST))
          .build();

  protected static final Integration INTEGRATION_CREATING = INTEGRATION_ACTIVE.toBuilder()
          .status(ZeroETLIntegrationStatus.CREATING)
          .build();

  protected static final Integration INTEGRATION_FAILED = INTEGRATION_ACTIVE.toBuilder()
          .status(ZeroETLIntegrationStatus.FAILED)
          .build();

  protected static final Integration INTEGRATION_DELETING = INTEGRATION_ACTIVE.toBuilder()
          .status(ZeroETLIntegrationStatus.DELETING)
          .build();

  protected static final ResourceModel INTEGRATION_ACTIVE_MODEL = ResourceModel.builder()
          .integrationArn(INTEGRATION_ARN)
          .integrationName(INTEGRATION_NAME)
          .sourceArn(SOURCE_ARN)
          .targetArn(TARGET_ARN)
          .kMSKeyId(KMS_KEY_ID)
          .createTime("2023-11-09T00:30:54.712000+00:00")
          .additionalEncryptionContext(ADDITIONAL_ENCRYPTION_CONTEXT)
          .tags(TAG_LIST)
          .build();

  protected static final ResourceModel INTEGRATION_MODEL_WITH_NO_NAME = INTEGRATION_ACTIVE_MODEL.toBuilder()
          .integrationName(null)
          .build();

  protected static Collection<software.amazon.awssdk.services.redshift.model.Tag> toAPITags(Collection<Tag> tags) {
    return tags.stream().map(t -> software.amazon.awssdk.services.redshift.model.Tag.builder()
                    .key(t.getKey())
                    .value(t.getValue())
                    .build())
            .collect(Collectors.toSet());
  }

  static <ClientT> ProxyClient<ClientT> MOCK_PROXY(final AmazonWebServicesClientProxy proxy, final ClientT client) {
    return new BaseProxyClient<>(proxy, client);
  }
}
