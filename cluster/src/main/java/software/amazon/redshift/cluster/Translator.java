package software.amazon.redshift.cluster;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.MapDifference;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AquaConfiguration;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupStatus;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotCopyStatus;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateSnapshotCopyGrantRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeferredMaintenanceWindow;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteSnapshotCopyGrantRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeSnapshotCopyGrantsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.DisableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.DisableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.ElasticIpStatus;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.Endpoint;
import software.amazon.awssdk.services.redshift.model.HsmStatus;
import software.amazon.awssdk.services.redshift.model.ModifyAquaConfigurationRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifySnapshotCopyRetentionPeriodRequest;
import software.amazon.awssdk.services.redshift.model.PauseClusterRequest;
import software.amazon.awssdk.services.redshift.model.RebootClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResizeClusterRequest;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.ResumeClusterRequest;
import software.amazon.awssdk.services.redshift.model.RotateEncryptionKeyRequest;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {
  private static String FINAL_SNAPSHOT_SUFFIX = "-final-snapshot";
  private static int ADD_IAM_ROLES_INDEX = 0;
  private static int REMOVE_IAM_ROLES_INDEX = 1;
  private static String CLUSTER_TYPE_SINGLE_NODE = "single-node";
  private static String CLUSTER_TYPE_MULTI_NODE = "multi-node";
  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static CreateClusterRequest translateToCreateRequest(final ResourceModel model) {
    return CreateClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .masterUsername(model.getMasterUsername())
            .masterUserPassword(model.getMasterUserPassword())
            .clusterType(model.getClusterType())
            .nodeType(model.getNodeType())
            .numberOfNodes(model.getNumberOfNodes())
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
            .availabilityZone(model.getAvailabilityZone())
            .clusterParameterGroupName(model.getClusterParameterGroupName())
            .clusterSecurityGroups(model.getClusterSecurityGroups())
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .clusterVersion(model.getClusterVersion())
            .dbName(model.getDBName())
            .elasticIp(model.getElasticIp())
            .encrypted(model.getEncrypted())
            .kmsKeyId(model.getKmsKeyId())
            .hsmClientCertificateIdentifier(model.getHsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(model.getHsmConfigurationIdentifier())
            .port(model.getPort())
            .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow())
            .publiclyAccessible(model.getPubliclyAccessible())
            .vpcSecurityGroupIds(model.getVpcSecurityGroupIds())
            .iamRoles(model.getIamRoles())
            .tags(translateTagsToSdk(model.getTags()))
            .availabilityZoneRelocation(model.getAvailabilityZoneRelocation())
            .aquaConfigurationStatus(model.getAquaConfigurationStatus())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .enhancedVpcRouting(model.getEnhancedVpcRouting())
            .maintenanceTrackName(model.getMaintenanceTrackName())
            .build();
  }

  /**
   * Request to enable logging properties
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static EnableLoggingRequest translateToEnableLoggingRequest(final ResourceModel model) {
    return EnableLoggingRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .bucketName(model.getLoggingProperties().getBucketName())
            .s3KeyPrefix(model.getLoggingProperties().getS3KeyPrefix())
            .build();
  }

  /**
   * Request to disable logging properties
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static DisableLoggingRequest translateToDisableLoggingRequest(final ResourceModel model) {
    return DisableLoggingRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Create Snapshot Copy Grant
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static CreateSnapshotCopyGrantRequest translateToCreateSnapshotCopyGrant(final ResourceModel model) {
    return CreateSnapshotCopyGrantRequest.builder()
            .snapshotCopyGrantName(model.getSnapshotCopyGrantName())
            .kmsKeyId(model.getKmsKeyId())
            .tags(translateTagsToSdk(model.getTags()))
            .build();
  }

  /**
   * Request to Delete Snapshot Copy Grant
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static DeleteSnapshotCopyGrantRequest translateToDeleteSnapshotCopyGrant(final ResourceModel model) {
    return DeleteSnapshotCopyGrantRequest.builder()
            .snapshotCopyGrantName(model.getSnapshotCopyGrantName())
            .build();
  }

  /**
   * Request to enable Cross-region cluster Snapshot
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static EnableSnapshotCopyRequest translateToEnableSnapshotRequest(final ResourceModel model) {
    return EnableSnapshotCopyRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .destinationRegion(model.getDestinationRegion())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .snapshotCopyGrantName(model.getSnapshotCopyGrantName())
            .retentionPeriod(model.getSnapshotCopyRetentionPeriod())
            .build();
  }

  /**
   * Request to Disable Cluster Snapshot
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static DisableSnapshotCopyRequest translateToDisableSnapshotRequest(final ResourceModel model) {
    return DisableSnapshotCopyRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Modify Snapshot Copy Retention Period
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifySnapshotCopyRetentionPeriodRequest translateToModifySnapshotCopyRetentionPeriodRequest(final ResourceModel model) {
    return ModifySnapshotCopyRetentionPeriodRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .retentionPeriod(model.getSnapshotCopyRetentionPeriod())
            .manual(model.getSnapshotCopyManual())
            .build();
  }

  /**
   * Request to Resize Cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ResizeClusterRequest translateToResizeClusterRequest(final ResourceModel model) {
    return ResizeClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .clusterType(model.getClusterType())
            .nodeType(model.getNodeType())
            .numberOfNodes(model.getNumberOfNodes())
            .classic(model.getClassic())
            .build();
  }

  /**
   * Request to Modify AQUA Configuration
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyAquaConfigurationRequest translateToModifyAquaConfigurationRequest(final ResourceModel model) {
    return ModifyAquaConfigurationRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .aquaConfigurationStatus(model.getAquaConfigurationStatus())
            .build();
  }

  /**
   * Request to Modify Cluster Maintenance
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterMaintenanceRequest translateToModifyClusterMaintenanceRequest(final ResourceModel model) {
    return ModifyClusterMaintenanceRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .deferMaintenance(model.getDeferMaintenance())
            .deferMaintenanceDuration(model.getDeferMaintenanceDuration())
            .deferMaintenanceIdentifier(model.getDeferMaintenanceIdentifier())
            .deferMaintenanceStartTime(model.getDeferMaintenanceStartTime() == null ? null : Instant.parse(model.getDeferMaintenanceStartTime()))
            .deferMaintenanceEndTime(model.getDeferMaintenanceEndTime() == null ? null : Instant.parse(model.getDeferMaintenanceEndTime()))
            .build();
  }

  /**
   * Request to Modify Cluster Db Revision
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterDbRevisionRequest translateToModifyClusterDbRevisionRequest(final ResourceModel model) {
    return ModifyClusterDbRevisionRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .revisionTarget(model.getRevisionTarget())
            .build();
  }

  /**
   * Request to describe logging properties
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static DescribeLoggingStatusRequest translateToDescribeStatusLoggingRequest(final ResourceModel model) {
    return DescribeLoggingStatusRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Translates DescribeLoggingStatusResponse object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromDescribeLoggingResponse(final DescribeLoggingStatusResponse awsResponse) {
      LoggingProperties loggingProperties = LoggingProperties.builder()
              .bucketName(awsResponse.bucketName())
              .s3KeyPrefix(awsResponse.s3KeyPrefix())
              .build();
      return ResourceModel.builder()
            .loggingProperties(loggingProperties)
            .build();
  }

  static List<String> translateTagKeysToSdk(final List<software.amazon.redshift.cluster.Tag> tags) {
     return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
             .map(software.amazon.redshift.cluster.Tag ::getKey)
             .collect(Collectors.toList());
  }

  static List<String> translateTagValuesToSdk(final List<software.amazon.redshift.cluster.Tag> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
            .map(software.amazon.redshift.cluster.Tag ::getValue)
            .collect(Collectors.toList());
  }

  static List<software.amazon.awssdk.services.redshift.model.Tag> translateTagsToSdk(final List<software.amazon.redshift.cluster.Tag> tags) {
      return Optional.ofNullable(tags).orElse(Collections.emptyList())
              .stream()
              .map(tag -> software.amazon.awssdk.services.redshift.model.Tag.builder()
                      .key(tag.getKey())
                      .value(tag.getValue()).build())
              .collect(Collectors.toList());
  }

  static List<software.amazon.redshift.cluster.Tag> translateTagsFromSdk(final List<software.amazon.awssdk.services.redshift.model.Tag> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList())
            .stream()
            .map(tag -> software.amazon.redshift.cluster.Tag.builder()
                    .key(tag.key())
                    .value(tag.value()).build())
            .collect(Collectors.toList());
  }

  static software.amazon.redshift.cluster.Endpoint translateEndpointFromSdk(final Endpoint endpoint) {
    return software.amazon.redshift.cluster.Endpoint.builder().port(endpoint.port().toString())
            .address(endpoint.address())
            .build();
  }

  static String translateDeferMaintenanceIdentifierFromSdk (List<DeferredMaintenanceWindow> deferMaintenanceWindows) {
    String deferMaintenanceIdentifier= deferMaintenanceWindows.stream().map(
            DeferredMaintenanceWindow::deferMaintenanceIdentifier).collect(Collectors.joining());

    return StringUtils.isNullOrEmpty(deferMaintenanceIdentifier) ? null : deferMaintenanceIdentifier;
  }

  static String translateDeferMaintenanceStartTimeFromSdk (List<DeferredMaintenanceWindow> deferMaintenanceWindows) {
    final Instant deferMaintenanceStartTime = deferMaintenanceWindows.stream().map(
            DeferredMaintenanceWindow::deferMaintenanceStartTime).filter(Objects::nonNull).findAny().orElse(null);
    return deferMaintenanceStartTime == null ? null : deferMaintenanceStartTime.toString();
  }

  static String translateDeferMaintenanceEndTimeFromSdk (List<DeferredMaintenanceWindow> deferMaintenanceWindows) {
    final Instant deferMaintenanceEndTime = deferMaintenanceWindows.stream().map(
            DeferredMaintenanceWindow::deferMaintenanceEndTime).filter(Objects::nonNull).findAny().orElse(null);
    return deferMaintenanceEndTime == null ? null : deferMaintenanceEndTime.toString();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClustersRequest translateToDescribeClusterRequest(final ResourceModel model) {
    return DescribeClustersRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Describe Snapshot Copy Grant
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeSnapshotCopyGrantsRequest translateToDescribeSnapshotCopyGrantsRequest(final ResourceModel model) {
    return DescribeSnapshotCopyGrantsRequest.builder()
            .snapshotCopyGrantName(model.getSnapshotCopyGrantName())
            .tagKeys(translateTagKeysToSdk(model.getTags()))
            .tagValues(translateTagValuesToSdk(model.getTags()))
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeClustersResponse awsResponse) {
    final String clusterIdentifier = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterIdentifier)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String masterUsername = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::masterUsername)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String nodeType = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::nodeType)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Integer numberOfNodes = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::numberOfNodes)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Boolean allowVersionUpgrade = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::allowVersionUpgrade)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Integer automatedSnapshotRetentionPeriod = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::automatedSnapshotRetentionPeriod)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Integer manualSnapshotRetentionPeriod = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::manualSnapshotRetentionPeriod)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);


    final String availabilityZone = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::availabilityZone)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterVersion = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterVersion)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String dbName = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::dbName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Boolean encrypted = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::encrypted)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);


    final String kmsKeyId = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::kmsKeyId)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String preferredMaintenanceWindow = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::preferredMaintenanceWindow)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Boolean publiclyAccessible = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::publiclyAccessible)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<ClusterSecurityGroupMembership> clusterSecurityGroups = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterSecurityGroups)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<ClusterIamRole> iamRoles = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::iamRoles)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<VpcSecurityGroupMembership> vpcSecurityGroupIds = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::vpcSecurityGroups)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<ClusterParameterGroupStatus> clusterParameterGroups = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterParameterGroups)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    List<String> clusterParameterGroupName = clusterParameterGroups.stream().map(
            clusterParameterGroupStatus -> clusterParameterGroupStatus.parameterGroupName()).
            collect(Collectors.toList());

    final String clusterSubnetGroupName = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterSubnetGroupName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final ElasticIpStatus elasticIp = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::elasticIpStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final HsmStatus hsmStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::hsmStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Endpoint endpoint = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::endpoint)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<software.amazon.awssdk.services.redshift.model.Tag> tags = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::tags)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final ClusterSnapshotCopyStatus clusterSnapshotCopyStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterSnapshotCopyStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String availabilityZoneRelocationStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::availabilityZoneRelocationStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final AquaConfiguration aquaConfiguration = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::aquaConfiguration)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterType = numberOfNodes == null || numberOfNodes < 2 ? CLUSTER_TYPE_SINGLE_NODE : CLUSTER_TYPE_MULTI_NODE;

    final Boolean enhanceVpcRouting = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::enhancedVpcRouting)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String maintenanceTrackName = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::maintenanceTrackName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<DeferredMaintenanceWindow> deferMaintenanceWindows = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::deferredMaintenanceWindows)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);


    return ResourceModel.builder()
            .clusterIdentifier(clusterIdentifier)
            .masterUsername(masterUsername)
            .nodeType(nodeType)
            .clusterType(clusterType)
            .numberOfNodes(numberOfNodes)
            .allowVersionUpgrade(allowVersionUpgrade == null ? null : allowVersionUpgrade.booleanValue())
            .automatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod)
            .availabilityZone(availabilityZone)
            .clusterVersion(clusterVersion)
            .encrypted(encrypted == null ? null : encrypted.booleanValue())
            .kmsKeyId(kmsKeyId)
            .preferredMaintenanceWindow(preferredMaintenanceWindow)
            .publiclyAccessible(publiclyAccessible == null ? null : publiclyAccessible.booleanValue())
            .clusterSecurityGroups(translateClusterSecurityGroupsFromSdk(clusterSecurityGroups))
            .iamRoles(translateIamRolesFromSdk(iamRoles))
            .vpcSecurityGroupIds(translateVpcSecurityGroupIdsFromSdk(vpcSecurityGroupIds))
            .clusterParameterGroupName(!CollectionUtils.isNullOrEmpty(clusterParameterGroupName)
                    ? clusterParameterGroupName.toString() : null)
            .clusterSubnetGroupName(clusterSubnetGroupName)
            .dBName(dbName)
            .elasticIp(elasticIp != null ? elasticIp.elasticIp() : null)
            .hsmClientCertificateIdentifier(hsmStatus != null ? hsmStatus.hsmClientCertificateIdentifier() : null)
            .hsmConfigurationIdentifier(hsmStatus != null ? hsmStatus.hsmConfigurationIdentifier() : null)
            .port(endpoint != null ? endpoint.port() : null)
            .endpoint(endpoint != null ? translateEndpointFromSdk(endpoint) : null)
            .tags(translateTagsFromSdk(tags))
            .destinationRegion(clusterSnapshotCopyStatus == null ? null : clusterSnapshotCopyStatus.destinationRegion() == null ? null : clusterSnapshotCopyStatus.destinationRegion())
            .manualSnapshotRetentionPeriod(manualSnapshotRetentionPeriod)
            .snapshotCopyRetentionPeriod(clusterSnapshotCopyStatus == null ? null :clusterSnapshotCopyStatus.retentionPeriod() == null ? null : clusterSnapshotCopyStatus.retentionPeriod().intValue())
            .snapshotCopyGrantName(clusterSnapshotCopyStatus == null ? null :clusterSnapshotCopyStatus.snapshotCopyGrantName() == null ? null : clusterSnapshotCopyStatus.snapshotCopyGrantName())
            .availabilityZoneRelocationStatus(availabilityZoneRelocationStatus)
            .aquaConfigurationStatus(aquaConfiguration == null ? null : aquaConfiguration.aquaConfigurationStatusAsString())
            .enhancedVpcRouting(enhanceVpcRouting == null ? null : enhanceVpcRouting.booleanValue())
            .maintenanceTrackName(maintenanceTrackName)
            .deferMaintenanceIdentifier(translateDeferMaintenanceIdentifierFromSdk(deferMaintenanceWindows))
            .deferMaintenanceStartTime(translateDeferMaintenanceStartTimeFromSdk(deferMaintenanceWindows))
            .deferMaintenanceEndTime(translateDeferMaintenanceEndTimeFromSdk(deferMaintenanceWindows))
            .build();
  }

  static List<String> translateClusterSecurityGroupsFromSdk (final List<ClusterSecurityGroupMembership> clusterSecurityGroups) {
    return clusterSecurityGroups.stream().map((clusterSecurityGroup ->
            clusterSecurityGroup.clusterSecurityGroupName())).collect(Collectors.toList());
  }

  static List<String> translateVpcSecurityGroupIdsFromSdk (final List<VpcSecurityGroupMembership> vpcSecurityGroupIds) {
    return vpcSecurityGroupIds.stream().map((vpcSecurityGroup ->
            vpcSecurityGroup.vpcSecurityGroupId())).collect(Collectors.toList());
  }

  static List<String> translateIamRolesFromSdk (final List<ClusterIamRole> iamRoles) {
    return iamRoles.stream().map((iamRole ->
            iamRole.iamRoleArn())).collect(Collectors.toList());
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterRequest translateToDeleteRequest(final ResourceModel model, Boolean snapshotRequested) {
    snapshotRequested = snapshotRequested != null && snapshotRequested;
    return DeleteClusterRequest
            .builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .skipFinalClusterSnapshot(!snapshotRequested)
            .finalClusterSnapshotIdentifier(finalClusterSnapshotIdentifierBuilder(model.getClusterIdentifier(),
                    snapshotRequested))
            .build();
  }

  static String finalClusterSnapshotIdentifierBuilder(String clusterIdentifier, boolean snapshotRequested) {
    if (snapshotRequested) {
      return clusterIdentifier + FINAL_SNAPSHOT_SUFFIX;
    }
    return null;
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterRequest translateToUpdateRequest(final ResourceModel model, final ResourceModel prevModel) {
    ModifyClusterRequest modifyClusterRequest =  ModifyClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .masterUserPassword(model.getMasterUserPassword().equals(prevModel.getMasterUserPassword()) ? null : model.getMasterUserPassword())
            .allowVersionUpgrade(model.getAllowVersionUpgrade() == null || model.getAllowVersionUpgrade().equals(prevModel.getAllowVersionUpgrade()) ? null : model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod() == null || model.getAutomatedSnapshotRetentionPeriod().equals(prevModel.getAutomatedSnapshotRetentionPeriod()) ? null : model.getAutomatedSnapshotRetentionPeriod())
            .clusterParameterGroupName(model.getClusterParameterGroupName() == null || model.getClusterParameterGroupName().equals(prevModel.getClusterParameterGroupName()) ? null : model.getClusterParameterGroupName())
            .clusterVersion(model.getClusterVersion() == null || model.getClusterVersion().equals(prevModel.getClusterVersion()) ? null : model.getClusterVersion())
            .hsmClientCertificateIdentifier(model.getHsmClientCertificateIdentifier() == null || model.getHsmClientCertificateIdentifier().equals(prevModel.getHsmClientCertificateIdentifier()) ? null : model.getHsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(model.getHsmConfigurationIdentifier() == null || model.getHsmConfigurationIdentifier().equals(prevModel.getHsmConfigurationIdentifier()) ? null : model.getHsmConfigurationIdentifier())
            .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow() == null || model.getPreferredMaintenanceWindow().equals(prevModel.getPreferredMaintenanceWindow()) ? null : model.getPreferredMaintenanceWindow())
            .publiclyAccessible(model.getPubliclyAccessible() == null || model.getPubliclyAccessible().equals(prevModel.getPubliclyAccessible()) ? null : model.getPubliclyAccessible())
            .clusterSecurityGroups(model.getClusterSecurityGroups() == null || model.getClusterSecurityGroups().equals(prevModel.getClusterSecurityGroups()) ? null : model.getClusterSecurityGroups())
            .vpcSecurityGroupIds(model.getVpcSecurityGroupIds() == null || model.getVpcSecurityGroupIds().equals(prevModel.getVpcSecurityGroupIds()) ? null : model.getVpcSecurityGroupIds())
            .availabilityZone(model.getAvailabilityZone() == null || model.getAvailabilityZone().equals(prevModel.getAvailabilityZone()) ? null : model.getAvailabilityZone())
            .availabilityZoneRelocation(model.getAvailabilityZoneRelocation() == null || model.getAvailabilityZoneRelocation().
                    equals(prevModel.getAvailabilityZoneRelocation()) ? null : model.getAvailabilityZoneRelocation())
            .encrypted(model.getEncrypted() == null || model.getEncrypted().equals(prevModel.getEncrypted()) ? null : model.getEncrypted())
            .kmsKeyId(model.getKmsKeyId() == null || model.getKmsKeyId().equals(prevModel.getKmsKeyId()) ? null : model.getKmsKeyId())
            .port(model.getPort() == null || model.getPort().equals(prevModel.getPort()) ? null : model.getPort())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod() == null || model.getManualSnapshotRetentionPeriod().equals(prevModel.getManualSnapshotRetentionPeriod()) ? null : model.getManualSnapshotRetentionPeriod())
            .clusterVersion(model.getClusterVersion() == null || model.getClusterVersion().equals(prevModel.getClusterVersion()) ? null : model.getClusterVersion())
            .elasticIp(model.getElasticIp() == null || model.getElasticIp().equals(prevModel.getElasticIp()) ? null : model.getElasticIp())
            .maintenanceTrackName(model.getMaintenanceTrackName() == null || model.getMaintenanceTrackName().equals(prevModel.getMaintenanceTrackName()) ? null : model.getMaintenanceTrackName())
            .enhancedVpcRouting(model.getEnhancedVpcRouting() == null || model.getEnhancedVpcRouting().equals(prevModel.getEnhancedVpcRouting()) ? null : model.getEnhancedVpcRouting())
            .build();

    return modifyClusterRequest;
  }

  /**
   * Request to update IAM of a previously created cluster
   * @param model resource model
   * @param iamRoleUpdate IAM roles to be updated
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterIamRolesRequest translateToUpdateIAMRolesRequest(final ResourceModel model, List<List<String>> iamRoleUpdate) {
    return ModifyClusterIamRolesRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .addIamRoles(iamRoleUpdate.get(ADD_IAM_ROLES_INDEX))
            .removeIamRoles(iamRoleUpdate.get(REMOVE_IAM_ROLES_INDEX))
            .build();
  }

  /**
   * Request to create Tags
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static CreateTagsRequest translateToCreateTagsRequest(ResourceModel model, List<Tag> createTags, String resourceName) {
    return CreateTagsRequest.builder()
            .tags(translateTagsToSdk(createTags))
            .resourceName(resourceName)
            .build();
  }

  /**
   * Request to delete Tags
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static DeleteTagsRequest translateToDeleteTagsRequest(ResourceModel model, List<Tag> deleteTags, String resourceName) {
    return DeleteTagsRequest.builder()
            .resourceName(resourceName)
            .tagKeys(deleteTags.stream().map(Tag::getKey).collect(Collectors.toList()))
            .build();
  }

  /**
   * Request to Reboot Cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static RebootClusterRequest translateToRebootClusterRequest(ResourceModel model) {
    return RebootClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Resume Cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ResumeClusterRequest translateToResumeClusterRequest(ResourceModel model) {
    return ResumeClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Pause Cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static PauseClusterRequest translateToPauseClusterRequest(ResourceModel model) {
    return PauseClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Rotate Encryption Key for Cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static RotateEncryptionKeyRequest translateToRotateEncryptionKeyRequest(ResourceModel model) {
    return RotateEncryptionKeyRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to update some other properties that could not be provisioned through first update request
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static AwsRequest translateToSecondUpdateRequest(final ResourceModel model) {
    final AwsRequest awsRequest = null;
    // TODO: construct a request
    return awsRequest;
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static DescribeClustersRequest translateToListRequest(final String nextToken) {
    return DescribeClustersRequest
            .builder()
            .marker(nextToken)
            .build();

  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListRequest(final DescribeClustersResponse awsResponse) {
    List<ResourceModel> resourceModels =  streamOfOrEmpty(awsResponse.clusters())
            .map(clusterIdentifier -> ResourceModel.builder()
                    .clusterIdentifier(clusterIdentifier.clusterIdentifier())
                    .build())
            .collect(Collectors.toList());
    return resourceModels;
  }

  /**
   * RestoreFromClusterSnapshot Request
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static RestoreFromClusterSnapshotRequest translateToRestoreFromClusterSnapshotRequest(final ResourceModel model) {
    return RestoreFromClusterSnapshotRequest.builder()
            .iamRoles(model.getIamRoles())
            .clusterIdentifier(model.getClusterIdentifier())
            .snapshotClusterIdentifier(model.getSnapshotClusterIdentifier())
            .snapshotIdentifier(model.getSnapshotIdentifier())
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .availabilityZone(model.getAvailabilityZone())
            .clusterSubnetGroupName(model.getClusterSubnetGroupName())
            .elasticIp(model.getElasticIp())
            .hsmClientCertificateIdentifier(model.getHsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(model.getHsmConfigurationIdentifier())
            .ownerAccount(model.getOwnerAccount())
            .port(model.getPort())
            .publiclyAccessible(model.getPubliclyAccessible())
            .clusterParameterGroupName(model.getClusterParameterGroupName())
            .clusterSecurityGroups(model.getClusterSecurityGroups())
            .vpcSecurityGroupIds(model.getVpcSecurityGroupIds())
            .availabilityZoneRelocation(model.getAvailabilityZoneRelocation())
            .aquaConfigurationStatus(model.getAquaConfigurationStatus())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .enhancedVpcRouting(model.getEnhancedVpcRouting())
            .maintenanceTrackName(model.getMaintenanceTrackName())
            .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow())
            .kmsKeyId(model.getKmsKeyId())
            .nodeType(model.getNodeType())
            .numberOfNodes(model.getNumberOfNodes())
            .build();
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }

  static DescribeClustersRequest describeClusterIdentifierRequest(final String clusterIdentifier) {
    return DescribeClustersRequest.builder()
            .clusterIdentifier(clusterIdentifier)
            .build();
  }

  static DescribeClustersResponse describeClustersResponse(final String clusterIdentifier, final AmazonWebServicesClientProxy proxy, final ProxyClient<RedshiftClient> proxyClient) {
    return proxyClient.injectCredentialsAndInvokeV2(Translator.describeClusterIdentifierRequest(clusterIdentifier), proxyClient.client()::describeClusters);
  }

  static ModifyClusterRequest modifyClusterNodeType(String desiredNodeType, String clusterIdentifier) {
    return ModifyClusterRequest
            .builder()
            .clusterIdentifier(clusterIdentifier)
            .nodeType(desiredNodeType).build();
  }

}
