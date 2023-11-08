package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AquaConfiguration;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupStatus;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotCopyStatus;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  private Logger logger;

  protected int CREATE_TAGS_INDEX = 0;
  protected int DELETE_TAGS_INDEX = 1;
  protected int ADD_IAM_ROLES_INDEX = 0;
  protected int DELETE_IAM_ROLES_INDEX = 1;
  protected  final String PARAMETER_GROUP_STATUS_PENDING_REBOOT = "pending-reboot";
  protected final String PARAMETER_GROUP_STATUS_IN_SYNC = "in-sync";
  protected final String CLUSTER_STATUS_AVAILABLE = "available";
  protected final String CLUSTER_STATUS_PAUSED = "paused";
  protected final String CLUSTER_STATUS_RESUME = "resume";
  protected final String AQUA_STATUS_APPLYING = "applying";
  protected final int CALLBACK_DELAY_SECONDS = 30;
  protected final int WAIT_TIME_IN_SECS_AFTER_INITIAL_MODIFY_CLUSTER_API_CALL = 10;
  private static boolean IS_CLUSTER_PATCHED = false;
  private final static int MAX_RETRIES_FOR_AQUA_CHECK = 6;
  private final static int MAX_RETRIES_FOR_PATCHING_CHECK = 6;

  protected static final Constant BACKOFF_STRATEGY = Constant.of().
          timeout(Duration.ofDays(5L)).delay(Duration.ofSeconds(10L)).build();
  protected static final String FAILOVER_PRIMARY_COMPUTE = "failover-primary-compute";
  protected static final String PAUSE_CLUSTER = "pause-cluster";
  protected static final String RESUME_CLUSTER = "resume-cluster";
  protected static final String ROTATE_ENCRYPTION_KEY = "rotate-encryption-key";

  protected static final Constant CREATE_BACKOFF_STRATEGY = Constant.of().
          timeout(Duration.ofMinutes(60L)).delay(Duration.ofSeconds(5L)).build();

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    this.logger = logger;
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


  protected boolean isClusterActive (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    DescribeClustersResponse awsResponse =
            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

    Cluster cluster = awsResponse.clusters()
            .stream()
            .findAny()
            .orElse(Cluster.builder().build());

    return CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterStatus()) &&
            CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterAvailabilityStatus());
  }

  protected boolean doesClusterExist(final ProxyClient<RedshiftClient> proxyClient, ResourceModel model,
                                     String clusterIdentifier) {
    DescribeClustersResponse awsResponse = null;
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(clusterIdentifier).build();
    try {
      awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
    } catch (final ClusterNotFoundException e) {
        return false;
    }
    return true;
  }

  protected boolean isClusterActiveAfterDelete (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    try {
      DescribeClustersResponse awsResponse =
              proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
    } catch (final ClusterNotFoundException e) {
      return true;
    }
    return false;
  }

  protected boolean stabilizeCluster(final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt, ResourceHandlerRequest<ResourceModel> request) {
    return isClusterActive(proxyClient, model, cxt);
  }

  protected boolean stabilizeClusterAfterClusterParameterGroupUpdate(final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
      return isClusterActiveAfterUpdateDbParameterGroup(proxyClient, model, cxt);
  }

  protected boolean isClusterActiveAfterUpdateDbParameterGroup (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    DescribeClustersResponse awsResponse =
            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

    Cluster cluster = awsResponse.clusters()
            .stream()
            .findAny()
            .orElse(Cluster.builder().build());

    String clusterParameterGroupApplyStatus = cluster.clusterParameterGroups()
            .stream()
            .findFirst()
            .map(ClusterParameterGroupStatus::parameterApplyStatus)
            .orElse(null);

    return CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterStatus()) &&
            CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterAvailabilityStatus()) &&
            PARAMETER_GROUP_STATUS_PENDING_REBOOT.equals(clusterParameterGroupApplyStatus);
  }

  protected boolean isAquaConfigurationStatusApplied (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest = null;
    DescribeClustersResponse awsResponse = null;
    if (cxt.getRetryForAquaStabilize() < MAX_RETRIES_FOR_AQUA_CHECK) {
      cxt.setRetryForAquaStabilize(cxt.getRetryForAquaStabilize() + 1);
      return false;
    }
    try {
      awsRequest = DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
      awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

      Cluster cluster = awsResponse.clusters()
              .stream()
              .findAny()
              .orElse(Cluster.builder().build());

      if (ObjectUtils.allNotNull(cluster.aquaConfiguration())) {
        return CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterStatus()) &&
                CLUSTER_STATUS_AVAILABLE.equalsIgnoreCase(cluster.clusterAvailabilityStatus());
      }
    }
    catch (final RedshiftException e) {
      if (e.awsErrorDetails().errorCode().equals("InternalFailure") ||
              e.awsErrorDetails().errorMessage().contains("An internal error has occurred"))
        return false;
    }
    return false;
  }

  protected boolean isClusterPatched(final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    if (cxt.getRetryForPatchingStabilize() < MAX_RETRIES_FOR_PATCHING_CHECK) {
      cxt.setRetryForPatchingStabilize(cxt.getRetryForPatchingStabilize() + 1);
      return false;
    }
    return isClusterActive(proxyClient,model, cxt);
  }

  protected boolean issueResizeClusterRequest(ResourceModel prevModel, ResourceModel model) {
    return  ObjectUtils.notEqual(prevModel.getNodeType(), model.getNodeType()) ||
            ObjectUtils.notEqual(prevModel.getNumberOfNodes(), model.getNumberOfNodes()) ||
            ObjectUtils.notEqual(prevModel.getClusterType(), model.getClusterType());
  }

  protected boolean issueModifyClusterMaintenanceRequest(ResourceModel prevModel, ResourceModel model) {
    return ObjectUtils.allNotNull(model.getDeferMaintenance()) ||
            ObjectUtils.allNotNull(model.getDeferMaintenanceDuration()) ||
            ObjectUtils.allNotNull(model.getDeferMaintenanceStartTime()) ||
            ObjectUtils.allNotNull(model.getDeferMaintenanceEndTime()) ||
            ObjectUtils.allNotNull(model.getDeferMaintenanceIdentifier());
  }

  // check for required parameters to not have null values
  protected boolean invalidCreateClusterRequest(ResourceModel model) {
    return model.getClusterIdentifier() == null || model.getNodeType() == null
            || model.getMasterUsername() == null || model.getMasterUserPassword() == null;
  }

  protected boolean issueModifyClusterParameterGroupRequest(ResourceModel prevModel, ResourceModel model) {
    return ObjectUtils.notEqual(prevModel.getClusterParameterGroupName(), model.getClusterParameterGroupName());
  }

  protected boolean issueModifySnapshotCopyRetentionPeriod(ResourceModel prevModel, ResourceModel model) {
    return ObjectUtils.notEqual(prevModel.getSnapshotCopyRetentionPeriod(), model.getSnapshotCopyRetentionPeriod()) ||
            ObjectUtils.notEqual(prevModel.getSnapshotCopyManual(), model.getSnapshotCopyManual());
  }

  protected List<List<String>> iamRoleUpdate (List<String> existingIamRoles, List<String> newIamRoles) {
    List<List<String>> iamRolesForUpdate = new LinkedList<>();
    existingIamRoles = CollectionUtils.isNullOrEmpty(existingIamRoles) ? new LinkedList<String>() : existingIamRoles;
    newIamRoles = CollectionUtils.isNullOrEmpty(newIamRoles) ? new LinkedList<String>() : newIamRoles;

    if (ObjectUtils.notEqual(existingIamRoles, newIamRoles)) {
      // Compute which iam roles we need to delete and add
      Set<String> iamRolesToRemove = Sets.difference(new HashSet<>(existingIamRoles), new HashSet<>(newIamRoles));
      Set<String> iamRolesToAdd = Sets.difference(new HashSet<>(newIamRoles), new HashSet<>(existingIamRoles));

      iamRolesForUpdate.add(new LinkedList<>(iamRolesToAdd));
      iamRolesForUpdate.add(new LinkedList<>(iamRolesToRemove));
    }
    return iamRolesForUpdate;
  }

  protected List<List<Tag>> updateTags (List<Tag> existingTags, List<Tag> newTags) {
    List<List<Tag>> tagsForUpdate = new LinkedList<>();
    //if tags are null, create an empty list for set difference
    existingTags = CollectionUtils.isNullOrEmpty(existingTags) ? new LinkedList<Tag>() : existingTags;
    newTags = CollectionUtils.isNullOrEmpty(newTags) ? new LinkedList<Tag>() : newTags;

    if (ObjectUtils.notEqual(existingTags, newTags)) {

      Set<Tag> tagsToDelete = Sets.difference(new HashSet<>(existingTags), new HashSet<>(newTags));
      Set<Tag> tagsToAdd = Sets.difference(new HashSet<>(newTags), new HashSet<>(existingTags));

      tagsForUpdate.add(new LinkedList<>(tagsToAdd));
      tagsForUpdate.add(new LinkedList<>(tagsToDelete));

    }
    return tagsForUpdate;
  }

  protected boolean isLoggingEnabled(ProxyClient<RedshiftClient> proxyClient, ResourceModel model) {
    DescribeLoggingStatusResponse describeLoggingStatusResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDescribeStatusLoggingRequest(model),
            proxyClient.client()::describeLoggingStatus);
    if(ObjectUtils.allNotNull(describeLoggingStatusResponse)) {
      return describeLoggingStatusResponse.loggingEnabled() != null && describeLoggingStatusResponse.loggingEnabled();
    }
    return false;
  }

  protected boolean isCrossRegionCopyEnabled(ProxyClient<RedshiftClient> proxyClient, ResourceModel model) {
    DescribeClustersResponse describeClustersResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDescribeClusterRequest(model),
            proxyClient.client()::describeClusters);

    List<Cluster> clusters = describeClustersResponse.clusters();
    if(!CollectionUtils.isNullOrEmpty(clusters)) {
      Cluster cluster = describeClustersResponse.clusters().get(0);
      ClusterSnapshotCopyStatus clusterSnapshotCopyStatus = cluster.clusterSnapshotCopyStatus();
      if (ObjectUtils.anyNotNull(clusterSnapshotCopyStatus)) {
        return !StringUtils.isNullOrEmpty(clusterSnapshotCopyStatus.destinationRegion());
      }
    }
    return false;
  }

  protected String destinationRegionForCrossRegionCopy(ProxyClient<RedshiftClient> proxyClient, ResourceModel model) {
    DescribeClustersResponse describeClustersResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDescribeClusterRequest(model),
            proxyClient.client()::describeClusters);

    List<Cluster> clusters = describeClustersResponse.clusters();
    if(!CollectionUtils.isNullOrEmpty(clusters)) {
      Cluster cluster = describeClustersResponse.clusters().get(0);
      ClusterSnapshotCopyStatus clusterSnapshotCopyStatus = cluster.clusterSnapshotCopyStatus();
      if (ObjectUtils.anyNotNull(clusterSnapshotCopyStatus)) {
        return clusterSnapshotCopyStatus.destinationRegion();
      }
    }
    return null;
  }

  protected boolean isRebootRequired(ResourceModel model, ProxyClient<RedshiftClient> proxyClient) {
    List<Cluster> clusters = proxyClient.injectCredentialsAndInvokeV2(
            Translator.translateToDescribeClusterRequest(model), proxyClient.client()::describeClusters)
            .clusters();
    if (!CollectionUtils.isNullOrEmpty(clusters)) {
      if (!CollectionUtils.isNullOrEmpty(clusters.get(0).clusterParameterGroups())) {
        return PARAMETER_GROUP_STATUS_PENDING_REBOOT.equals(clusters.get(0).clusterParameterGroups().get(0)
                .parameterApplyStatus());
      }
    }
    return false;
  }

  protected boolean isAQUAStatusApplying(ResourceModel model, ProxyClient<RedshiftClient> proxyClient) {
    List<Cluster> clusters = proxyClient.injectCredentialsAndInvokeV2(
            Translator.translateToDescribeClusterRequest(model), proxyClient.client()::describeClusters)
            .clusters();
    if (!CollectionUtils.isNullOrEmpty(clusters)) {
      AquaConfiguration aquaConfiguration = clusters.get(0).aquaConfiguration();
      if (ObjectUtils.allNotNull(aquaConfiguration)) {
        return AQUA_STATUS_APPLYING.equals(aquaConfiguration.aquaStatusAsString());
      }
    }
    return false;
  }

  protected boolean isClusterPaused (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    DescribeClustersResponse awsResponse =
            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

    List<Cluster> clusters = awsResponse.clusters();
    if(!CollectionUtils.isNullOrEmpty(clusters)) {
      return CLUSTER_STATUS_PAUSED.equals(awsResponse.clusters().get(0).clusterStatus());
    }
    return false;
  }

  // with the existing dependencies,
  // the static method takes some effort to unit test,
  // will cover it later when upgrading the dependencies
  protected void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      throw new CfnGeneralServiceException("Please retry again in a few seconds, ", e);
    }

  }
}
