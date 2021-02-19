package software.amazon.redshift.cluster;

import com.amazonaws.SdkClientException;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
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

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  protected int CREATE_TAGS_INDEX = 0;
  protected int DELETE_TAGS_INDEX = 1;
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


//  protected boolean isClusterActiveAfterModify (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
//    String clusterIdentifier = StringUtils.isNullOrEmpty(model.getNewClusterIdentifier())
//            ? model.getClusterIdentifier() : model.getNewClusterIdentifier();
//
//    DescribeClustersRequest awsRequest =
//            DescribeClustersRequest.builder().clusterIdentifier(clusterIdentifier).build();
//    DescribeClustersResponse awsResponse =
//            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
//
//    return awsResponse.clusters().get(0).clusterStatus().equals("available");
//  }

  protected boolean isClusterActive (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model, CallbackContext cxt) {
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(model.getClusterIdentifier()).build();
    DescribeClustersResponse awsResponse =
            proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);

    return awsResponse.clusters().get(0).clusterStatus().equals("available");
  }

  protected boolean isClusterAvailableForUpdate (final ProxyClient<RedshiftClient> proxyClient, ResourceModel model,
                                                 String clusterIdentifier) {
    DescribeClustersResponse awsResponse = null;
    DescribeClustersRequest awsRequest =
            DescribeClustersRequest.builder().clusterIdentifier(clusterIdentifier).build();
    try {
      awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusters);
    } catch (final ClusterNotFoundException e) {
//        if (!StringUtils.isNullOrEmpty(model.getNewClusterIdentifier())) {
//          return isClusterAvailableForUpdate(proxyClient, model, model.getNewClusterIdentifier());
//        }
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

  protected boolean issueModifyClusterRequest(ResourceModel model) {
    return model.getNodeType() != null || model.getNumberOfNodes() != null || model.getAllowVersionUpgrade() != null ||
            model.getAutomatedSnapshotRetentionPeriod() != null || model.getClusterParameterGroupName() != null ||
            model.getClusterType() != null || model.getClusterVersion() != null ||
            model.getElasticIp() != null || model.getEncrypted() != null || model.getHsmClientCertificateIdentifier() != null ||
            model.getHsmConfigurationIdentifier() != null || model.getMasterUserPassword() != null ||
            model.getKmsKeyId() != null || model.getPreferredMaintenanceWindow() != null || model.getPubliclyAccessible() != null ||
            model.getClusterSecurityGroups() != null || model.getVpcSecurityGroupIds() != null;
  }

  protected List<List<String>> iamRoleUpdate (ResourceHandlerRequest<ResourceModel> request, ResourceModel model) {
    List<List<String>> iamRolesForUpdate = new LinkedList<>();
    if (ObjectUtils.notEqual(request.getPreviousResourceState().getIamRoles(), model.getIamRoles())) {
      Set<String> iamRolesSetPrevious = new HashSet<>(request.getPreviousResourceState().getIamRoles());
      Set<String> iamRolesSetCurrent = new HashSet<>(model.getIamRoles());

      // Compute which iam roles we need to delete and add
      Set<String> iamRolesToRemove = Sets.difference(new HashSet<>(request.getPreviousResourceState().getIamRoles()), new HashSet<>(model.getIamRoles()));
      Set<String> iamRolesToAdd = Sets.difference(new HashSet<>(model.getIamRoles()), new HashSet<>(request.getPreviousResourceState().getIamRoles()));

      iamRolesForUpdate.add(new LinkedList<>(iamRolesToAdd));
      iamRolesForUpdate.add(new LinkedList<>(iamRolesToRemove));
    }
    return iamRolesForUpdate;
  }

  protected List<List<Tag>> updateTags (List<Tag> existingTags, List<Tag> newTags) {
    List<List<Tag>> tagsForUpdate = new LinkedList<>();
    if (ObjectUtils.notEqual(existingTags, newTags)) {

      Set<Tag> tagsToDelete = Sets.difference(new HashSet<>(existingTags), new HashSet<>(newTags));
      Set<Tag> tagsToAdd = Sets.difference(new HashSet<>(newTags), new HashSet<>(existingTags));

      tagsForUpdate.add(new LinkedList<>(tagsToAdd));
      tagsForUpdate.add(new LinkedList<>(tagsToDelete));

    }
    return tagsForUpdate;
  }

}
