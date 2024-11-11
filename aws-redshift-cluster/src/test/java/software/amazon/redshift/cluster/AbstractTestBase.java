package software.amazon.redshift.cluster;

import com.google.common.collect.ImmutableList;
import java.lang.UnsupportedOperationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;
  protected static final String AWS_REGION;
  protected static final String AWS_PARTITION;
  protected static final String AWS_ACCOUNT_ID;
  protected static final String CLUSTER_IDENTIFIER;
  protected static final String SNAPSHOT_IDENTIFIER;
  protected static final String CLUSTER_NAMESPACE_UUID;
  protected static final String MASTER_USERNAME;
  protected static final String MASTER_USERPASSWORD;
  protected static final String MASTER_USERPASSWORD_SECRET_ARN;
  protected static final String NODETYPE;
  protected static final int NUMBER_OF_NODES;
  protected static final String BUCKET_NAME;
  protected static final String IAM_ROLE_ARN;
  protected static final String CLUSTER_NAMESPACE_ARN;
  protected static final String NAMESPACE_POLICY;
  protected static final String NAMESPACE_POLICY_EMPTY;
  protected static final String LOG_DESTINATION_TYPE_CW;
  protected static final ResourcePolicy RESOURCE_POLICY;
  protected static final ResourcePolicy RESOURCE_POLICY_EMPTY;
  protected static final LoggingProperties LOGGING_PROPERTIES_S3;
  protected static final LoggingProperties LOGGING_PROPERTIES_CW;
  protected static final LoggingProperties LOGGING_PROPERTIES_DISABLED;
  protected static final software.amazon.awssdk.services.redshift.model.Tag TAG;
  protected static final Integer DEFER_MAINTENANCE_DURATION;
  protected static final String DEFER_MAINTENANCE_IDENTIFIER;
  protected static final String DEFER_MAINTENANCE_START_TIME;
  protected static final String DEFER_MAINTENANCE_END_TIME;
  protected static final List<String> LOG_EXPORTS_TYPES;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
    AWS_REGION = "us-east-1";
    AWS_PARTITION = "aws";
    AWS_ACCOUNT_ID ="1111";
    CLUSTER_IDENTIFIER = "redshift-cluster-1";
    CLUSTER_NAMESPACE_UUID = "a1234-b5678";
    MASTER_USERNAME = "master";
    MASTER_USERPASSWORD = "Test1234";
    NODETYPE = "ds2.xlarge";
    NUMBER_OF_NODES = 2;
    BUCKET_NAME = "bucket-enable-logging";
    IAM_ROLE_ARN = "arn:aws:iam::" + AWS_ACCOUNT_ID + ":role/cfn_migration_test_IAM_role";
    CLUSTER_NAMESPACE_ARN = "arn:aws:redshift:" + AWS_REGION + ":" + AWS_ACCOUNT_ID + ":namespace/" + CLUSTER_NAMESPACE_UUID;
    MASTER_USERPASSWORD_SECRET_ARN = "arn:aws:secretsmanager:" + AWS_REGION + ":" + AWS_ACCOUNT_ID + ":secret/" + CLUSTER_NAMESPACE_UUID;
    NAMESPACE_POLICY = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Resource\": \"*\",\"Action\":\"test:test\"}]}";
    NAMESPACE_POLICY_EMPTY = "{}";
    DEFER_MAINTENANCE_DURATION = 40;
    DEFER_MAINTENANCE_IDENTIFIER = "cfn-defer-maintenance-identifier";
    DEFER_MAINTENANCE_START_TIME = "2023-12-10T00:00:00Z";
    DEFER_MAINTENANCE_END_TIME = "2024-01-19T00:00:00Z";
    SNAPSHOT_IDENTIFIER = "redshift-cluster-1-snapshot";
    LOG_DESTINATION_TYPE_CW = "cloudwatch";
    LOG_EXPORTS_TYPES = ImmutableList.of("connectionlog", "useractivitylog", "userlog");

    RESOURCE_POLICY = ResourcePolicy.builder()
            .resourceArn(CLUSTER_NAMESPACE_ARN)
            .policy(NAMESPACE_POLICY)
            .build();

    RESOURCE_POLICY_EMPTY = ResourcePolicy.builder()
            .resourceArn(CLUSTER_NAMESPACE_ARN)
            .policy(null)
            .build();


    LOGGING_PROPERTIES_S3 = LoggingProperties.builder()
            .bucketName(BUCKET_NAME)
            .s3KeyPrefix("test")
            .build();

    LOGGING_PROPERTIES_CW = LoggingProperties.builder()
            .logDestinationType(LOG_DESTINATION_TYPE_CW)
            .logExports(LOG_EXPORTS_TYPES)
            .build();

    LOGGING_PROPERTIES_DISABLED = LoggingProperties.builder()
            .logDestinationType(null)
            .logExports(new ArrayList<>())
            .bucketName(null)
            .s3KeyPrefix(null)
            .build();

    TAG = software.amazon.awssdk.services.redshift.model.Tag.builder()
            .key("foo")
            .value("bar")
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

  static ProxyClient<SecretsManagerClient> MOCK_PROXY_SECRETS_MANAGER (
          final AmazonWebServicesClientProxy proxy,
          final SecretsManagerClient sdkClient) {
    return new ProxyClient<SecretsManagerClient>() {
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
      public SecretsManagerClient client() {
        return sdkClient;
      }
    };
  }

  public static Cluster basicCluster(){
    return Cluster.builder()
            .clusterStatus("available")
            .clusterAvailabilityStatus("Available")
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .allowVersionUpgrade(true)
            .encrypted(false)
            .publiclyAccessible(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .automatedSnapshotRetentionPeriod(0)
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroups(Collections.emptyList())
            .build();
  }
  public static Cluster responseCluster() {
    return basicCluster().toBuilder()
            .clusterNamespaceArn(CLUSTER_NAMESPACE_ARN)
            .build();
  }

  public static Cluster responseManagedPasswordCluster() {
    return basicCluster().toBuilder()
            .clusterNamespaceArn(CLUSTER_NAMESPACE_ARN)
            .masterPasswordSecretArn(MASTER_USERPASSWORD_SECRET_ARN)
            .build();
  }

  public static ResourceModel createClusterRequestModel() {
    return ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .masterUserPassword(MASTER_USERPASSWORD)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterType("multi-node")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroupIds(Collections.emptyList())
            .tags(Collections.emptyList())
            .build();
  }

  public static ResourceModel restoreClusterRequestModel() {
    return createClusterRequestModel().toBuilder()
            .snapshotIdentifier(SNAPSHOT_IDENTIFIER)
            .build();
  }

  public static ResourceModel createClusterResponseModel() {
    return ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .clusterNamespaceArn(CLUSTER_NAMESPACE_ARN)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterType("multi-node")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroupIds(Collections.emptyList())
            .tags(Collections.emptyList())
            .build();
  }

  public static ResourceModel createClusterManagedPasswordResponseModel() {
    return ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .clusterNamespaceArn(CLUSTER_NAMESPACE_ARN)
            .masterUsername(MASTER_USERNAME)
            .masterPasswordSecretArn(MASTER_USERPASSWORD_SECRET_ARN)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterType("multi-node")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroupIds(Collections.emptyList())
            .tags(Collections.emptyList())
            .build();
  }

  public static DescribeClustersResponse describeClustersManagedPasswordResponseSdk() {

    return DescribeClustersResponse.builder()
            .clusters(responseCluster())
            .build();
  }

  public static CreateClusterResponse createClusterResponseSdk() {
    return CreateClusterResponse.builder()
            .cluster(responseCluster())
            .build();
  }

  public static EnableLoggingResponse createS3EnableLoggingResponseSdk() {
    return EnableLoggingResponse.builder()
            .bucketName(BUCKET_NAME)
            .loggingEnabled(true)
            .lastSuccessfulDeliveryTime(Instant.now())
            .build();
  }

  public static EnableLoggingResponse createCWEnableLoggingResponseSdk() {
    return EnableLoggingResponse.builder()
            .logDestinationType(LOG_DESTINATION_TYPE_CW)
            .logExports(LOG_EXPORTS_TYPES)
            .loggingEnabled(true)
            .lastSuccessfulDeliveryTime(Instant.now())
            .build();
  }

  public static DescribeClustersResponse describeClustersResponseSdk() {
    return DescribeClustersResponse.builder()
            .clusters(responseCluster())
            .build();
  }

  public static DescribeClustersResponse describeManagedPasswordClustersResponseSdk() {
    return DescribeClustersResponse.builder()
            .clusters(responseManagedPasswordCluster())
            .build();
  }

  public static DescribeLoggingStatusResponse describeLoggingStatusFalseResponseSdk() {
    return DescribeLoggingStatusResponse.builder()
            .loggingEnabled(false)
            .build();
  }

  public static PutResourcePolicyResponse putResourcePolicyResponseSdk() {
    return PutResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY)
            .build();
  }

  public static GetResourcePolicyResponse getResourcePolicyResponseSdk() {
    return GetResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY)
            .build();
  }

  public static DeleteResourcePolicyResponse deleteResourcePolicyResponseSdk() {
    return DeleteResourcePolicyResponse.builder().build();
  }

  public static PutResourcePolicyResponse putEmptyResourcePolicyResponseSdk() {
    return PutResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY_EMPTY)
            .build();
  }

  public static GetResourcePolicyResponse getEmptyResourcePolicyResponseSdk() {
    return GetResourcePolicyResponse.builder()
            .resourcePolicy(RESOURCE_POLICY_EMPTY)
            .build();
  }

  public static DeferredMaintenanceWindow deferredMaintenanceWindow() {
    return DeferredMaintenanceWindow.builder()
            .deferMaintenanceIdentifier(DEFER_MAINTENANCE_IDENTIFIER)
            .deferMaintenanceStartTime(Instant.parse(DEFER_MAINTENANCE_START_TIME))
            .deferMaintenanceEndTime(Instant.parse(DEFER_MAINTENANCE_END_TIME))
            .build();
  }

  public static ModifyClusterMaintenanceResponse getModifyClusterMaintenanceResponseSdk() {
    return ModifyClusterMaintenanceResponse.builder()
            .cluster(responseCluster().toBuilder()
                    .deferredMaintenanceWindows(deferredMaintenanceWindow())
                    .build())
            .build();
  }

  public static DescribeClustersResponse describeClustersResponseWithDeferMaintenanceSdk() {
    return DescribeClustersResponse.builder()
            .clusters(responseCluster().toBuilder()
                    .deferredMaintenanceWindows(deferredMaintenanceWindow())
                    .build())
            .build();
  }
}
