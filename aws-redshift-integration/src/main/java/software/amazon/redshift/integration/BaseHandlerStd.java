package software.amazon.redshift.integration;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.Integration;
import software.amazon.awssdk.services.redshift.model.ZeroETLIntegrationStatus;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;
import static software.amazon.redshift.integration.Tagging.translateTags;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  protected static final String STACK_NAME = "redshift";
  protected static final String RESOURCE_IDENTIFIER = "integration";
  protected static final String CLUSTER_STATUS_AVAILABLE = "available";
  protected static final String CLUSTER_AVAILABILITY_STATUS_AVAILABLE = "available";
  protected static final int MAX_LENGTH_INTEGRATION = 63;

  private static final String UTC = "UTC";
  private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";

  public static String formatDateTime(Date date) {
    TimeZone tzUtc = TimeZone.getTimeZone(UTC);
    DateFormat dateTimeFormatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    dateTimeFormatter.setTimeZone(tzUtc);
    return dateTimeFormatter.format(date);
  }

  /** Custom handler config, mostly to facilitate faster unit test */
  final HandlerConfig config;

  public BaseHandlerStd() {
    this(HandlerConfig.builder().build());
  }

  BaseHandlerStd(HandlerConfig config) {
    this.config = config;
  }

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<RedshiftClient> proxyClient,
    final Logger logger);

  /**
   * Integration is stablized when it's in active state.
   * @param model
   * @param proxyClient
   * @return
   */
  protected boolean isStabilized(final ResourceModel model, final ProxyClient<RedshiftClient> proxyClient) {
    final ZeroETLIntegrationStatus status = proxyClient.injectCredentialsAndInvokeV2(
                    ReadHandler.describeIntegrationsRequest(model),
                    proxyClient.client()::describeIntegrations)
            .integrations().stream().findFirst().get().status();

    if (!IntegrationStatusUtil.isValidCreatingStatus(status)) {
      throw new CfnNotStabilizedException(
              new Exception("Integration is in state a state that cannot complete creation: " + status));
    }

    return IntegrationStatusUtil.isStabilizedState(status) && isRedshiftClusterActive(model, proxyClient);
  }

  protected boolean isRedshiftClusterActive(final ResourceModel model, final ProxyClient<RedshiftClient> proxyClient) {
    final Cluster targetCluster = proxyClient.injectCredentialsAndInvokeV2(
                    DescribeClustersRequest.builder().build(),
                    proxyClient.client()::describeClusters).clusters().stream()
            .filter(cluster -> cluster.clusterNamespaceArn().equalsIgnoreCase(model.getTargetArn()))
            .findFirst()
            .orElse(null);

    if (targetCluster == null) {
      throw new CfnNotStabilizedException(
              new Exception("Target Redshift data warehouse does not exist, please check and retry"));
    }

    return targetCluster.clusterStatus().equalsIgnoreCase(CLUSTER_STATUS_AVAILABLE)
            && targetCluster.clusterAvailabilityStatus().equalsIgnoreCase(CLUSTER_AVAILABILITY_STATUS_AVAILABLE);
  }

  static ResourceModel translateToModel(
          final Integration integration
  ) {
    return ResourceModel.builder()
            .createTime(
                    Optional.ofNullable(integration.createTime())
                            .map(Date::from)
                            .map(BaseHandlerStd::formatDateTime)
                            .orElse(null))
            .sourceArn(integration.sourceArn())
            .integrationArn(integration.integrationArn())
            .integrationName(integration.integrationName())
            .targetArn(integration.targetArn())
            .kMSKeyId(integration.kmsKeyId())
            .tags(translateTags(integration.tags()))
            .additionalEncryptionContext(integration.additionalEncryptionContext())
            .build();
  }

  protected ProgressEvent<ResourceModel, CallbackContext> fetchIntegrationArn(final AmazonWebServicesClientProxy proxy,
                                                                              final ProxyClient<RedshiftClient> proxyClient,
                                                                              final ProgressEvent<ResourceModel, CallbackContext> progress) {
    return proxy.initiate("rds::read-integration-arn", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
            .translateToServiceRequest(ReadHandler::describeIntegrationsRequest)
            .makeServiceCall((describeIntegrationsRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(describeIntegrationsRequest, proxyInvocation.client()::describeIntegrations))
            .handleError((describeIntegrationsRequest, exception, client, resourceModel, ctx) -> handleIntegrationException(exception))
            .done((describeIntegrationsRequest, describeIntegrationsResponse, proxyInvocation, resourceModel, context) -> {
              final String arn = describeIntegrationsResponse.integrations().stream().findFirst().get().integrationArn();
              context.setIntegrationArn(arn);
              return ProgressEvent.progress(resourceModel, context);
            });
  }

  // with the existing dependencies,
  // the static method takes some effort to unit test,
  // will cover it later when upgrading the dependencies
  protected void sleepInSeconds(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      throw new CfnGeneralServiceException("Please retry again in a few seconds, ", e);
    }
  }
}
