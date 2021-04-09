package software.amazon.redshift.cluster;

import com.amazonaws.SdkClientException;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  protected int CREATE_TAGS_INDEX = 0;
  protected int DELETE_TAGS_INDEX = 1;
  protected int ADD_IAM_ROLES_INDEX = 0;
  protected int DELETE_IAM_ROLES_INDEX = 1;
  protected  final String PARAMETER_GROUP_STATUS_PENDING_REBOOT = "pending-reboot";
  protected final String PARAMETER_GROUP_STATUS_IN_SYNC = "in-sync";
  protected final String CLUSTER_STATUS_AVAILABLE = "available";
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


  protected boolean isClusterActive (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    DescribeClustersResponse awsResponse =
            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

    List<Cluster> clusters = awsResponse.clusters();
    if(!CollectionUtils.isNullOrEmpty(clusters)) {
      return CLUSTER_STATUS_AVAILABLE.equals(awsResponse.clusters().get(0).clusterStatus());
    }
    return false;
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


  // check for required parameters to not have null values
  protected boolean invalidCreateClusterRequest(ResourceModel model) {
    return model.getClusterIdentifier() == null || model.getNodeType() == null
            || model.getMasterUsername() == null || model.getMasterUserPassword() == null;
  }

  protected boolean issueModifyClusterRequest(ResourceModel prevModel, ResourceModel model) {
    return  ObjectUtils.notEqual(prevModel.getNodeType(), model.getNodeType()) ||
            ObjectUtils.notEqual(prevModel.getNumberOfNodes(), model.getNumberOfNodes()) ||
            ObjectUtils.notEqual(prevModel.getMasterUserPassword(), model.getMasterUserPassword()) ||
            ObjectUtils.notEqual(prevModel.getAllowVersionUpgrade(), model.getAllowVersionUpgrade()) ||
            ObjectUtils.notEqual(prevModel.getAutomatedSnapshotRetentionPeriod(), model.getAutomatedSnapshotRetentionPeriod()) ||
            ObjectUtils.notEqual(prevModel.getClusterParameterGroupName(), model.getClusterParameterGroupName()) ||
            ObjectUtils.notEqual(prevModel.getClusterType(), model.getClusterType()) ||
            ObjectUtils.notEqual(prevModel.getClusterVersion(), model.getClusterVersion()) ||
            ObjectUtils.notEqual(prevModel.getElasticIp(), model.getElasticIp()) ||
            ObjectUtils.notEqual(prevModel.getEncrypted(), model.getEncrypted()) ||
            ObjectUtils.notEqual(prevModel.getHsmClientCertificateIdentifier(), model.getHsmClientCertificateIdentifier()) ||
            ObjectUtils.notEqual(prevModel.getHsmConfigurationIdentifier(), model.getHsmConfigurationIdentifier()) ||
            ObjectUtils.notEqual(prevModel.getKmsKeyId(), model.getKmsKeyId()) ||
            ObjectUtils.notEqual(prevModel.getPreferredMaintenanceWindow(), model.getPreferredMaintenanceWindow()) ||
            ObjectUtils.notEqual(prevModel.getPubliclyAccessible(), model.getPubliclyAccessible()) ||
            ObjectUtils.notEqual(prevModel.getClusterSecurityGroups(), model.getClusterSecurityGroups()) ||
            ObjectUtils.notEqual(prevModel.getVpcSecurityGroupIds(), model.getVpcSecurityGroupIds());
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
}
