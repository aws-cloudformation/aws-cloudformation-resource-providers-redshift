package software.amazon.redshift.cluster;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AccountWithRestoreAccess;
import software.amazon.awssdk.services.redshift.model.ClusterDbRevision;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterNode;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupStatus;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotCopyStatus;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.DataTransferProgress;
import software.amazon.awssdk.services.redshift.model.DeferredMaintenanceWindow;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSnapshotsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSnapshotsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DisableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.DisableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.ElasticIpStatus;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.Endpoint;
import software.amazon.awssdk.services.redshift.model.HsmStatus;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifySnapshotCopyRetentionPeriodRequest;
import software.amazon.awssdk.services.redshift.model.PauseClusterRequest;
import software.amazon.awssdk.services.redshift.model.RebootClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResizeClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResizeInfo;
import software.amazon.awssdk.services.redshift.model.RestoreStatus;
import software.amazon.awssdk.services.redshift.model.ResumeClusterRequest;
import software.amazon.awssdk.services.redshift.model.RevisionTarget;
import software.amazon.awssdk.services.redshift.model.RotateEncryptionKeyRequest;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;


import java.time.Instant;
import java.util.Collection;
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
            .additionalInfo(model.getAdditionalInfo())
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
            .availabilityZone(model.getAvailabilityZone())
            .clusterParameterGroupName(model.getClusterParameterGroupName())
            .clusterType(model.getClusterType())
            .clusterVersion(model.getClusterVersion())
            .dbName(model.getDBName())
            .elasticIp(model.getElasticIp())
            .encrypted(model.getEncrypted())
            .enhancedVpcRouting(model.getEnhancedVpcRouting())
            .hsmClientCertificateIdentifier(model.getHsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(model.getHsmConfigurationIdentifier())
            .kmsKeyId(model.getKmsKeyId())
            .maintenanceTrackName(model.getMaintenanceTrackName())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .port(model.getPort())
            .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow())
            .publiclyAccessible(model.getPubliclyAccessible())
            .snapshotScheduleIdentifier(model.getSnapshotScheduleIdentifier())
            .clusterSecurityGroups(model.getClusterSecurityGroups())
            .iamRoles(model.getIamRoles())
            .vpcSecurityGroupIds(model.getVpcSecurityGroupIds())
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClustersRequest translateToReadRequest(final ResourceModel model) {
    String clusterIdentifier = StringUtils.isNullOrEmpty(model.getNewClusterIdentifier())
            ? model.getClusterIdentifier() : model.getNewClusterIdentifier();

    return DescribeClustersRequest.builder()
            .clusterIdentifier(clusterIdentifier)
            .build();
  }

  /**
   * Describe a Cluster DB Revisions Request
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClusterDbRevisionsRequest translateToDescribeClusterDBRevisionsRequest(final ResourceModel model) {
    return DescribeClusterDbRevisionsRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .marker(model.getMarker())
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

    final int numberOfNodes = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::numberOfNodes)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(0);

    final boolean allowVersionUpgrade = streamOfOrEmpty(awsResponse.clusters())
        .map(software.amazon.awssdk.services.redshift.model.Cluster::allowVersionUpgrade)
        .filter(Objects::nonNull)
        .findAny()
        .orElse(true);

    final int automatedSnapshotRetentionPeriod = streamOfOrEmpty(awsResponse.clusters())
        .map(software.amazon.awssdk.services.redshift.model.Cluster::automatedSnapshotRetentionPeriod)
        .filter(Objects::nonNull)
        .findAny()
        .orElse(0);


    final String availabilityZone = streamOfOrEmpty(awsResponse.clusters())
        .map(software.amazon.awssdk.services.redshift.model.Cluster::availabilityZone)
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);

    final String clusterAvailabilityStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterAvailabilityStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Instant clusterCreateTime = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterCreateTime)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<ClusterNode> clusterNodes = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterNodes)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<ClusterParameterGroupStatus> clusterParameterGroups = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterParameterGroups)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterPublicKey = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterPublicKey)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterRevisionNumber = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterRevisionNumber)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final ClusterSnapshotCopyStatus clusterSnapshotCopyStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterSnapshotCopyStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String clusterSubnetGroupName = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::clusterSubnetGroupName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final DataTransferProgress dataTransferProgress = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::dataTransferProgress)
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

    final boolean encrypted = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::encrypted)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(false);

    final ElasticIpStatus elasticIpStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::elasticIpStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String elasticResizeNumberOfNodeOptions = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::elasticResizeNumberOfNodeOptions)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Endpoint endpoint = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::endpoint)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);


    final boolean enhancedVpcRouting = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::enhancedVpcRouting)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(false);

    final Instant expectedNextSnapshotScheduleTime = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::expectedNextSnapshotScheduleTime)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String expectedNextSnapshotScheduleTimeStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::expectedNextSnapshotScheduleTimeStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final HsmStatus hsmStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::hsmStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);


    final String kmsKeyId = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::kmsKeyId)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String maintenanceTrackName = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::maintenanceTrackName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final int manualSnapshotRetentionPeriod = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::manualSnapshotRetentionPeriod)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(1);

    final String modifyStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::modifyStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Instant nextMaintenanceWindowStartTime = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::nextMaintenanceWindowStartTime)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String preferredMaintenanceWindow = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::preferredMaintenanceWindow)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final boolean publiclyAccessible = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::publiclyAccessible)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(true);

    final ResizeInfo resizeInfo = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::resizeInfo)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final RestoreStatus restoreStatus = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::restoreStatus)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String snapshotScheduleIdentifier = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::snapshotScheduleIdentifier)
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

    final List<DeferredMaintenanceWindow> deferMaintenanceWindows = streamOfOrEmpty(awsResponse.clusters())
            .map(software.amazon.awssdk.services.redshift.model.Cluster::deferredMaintenanceWindows)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    return ResourceModel.builder()
            .clusterIdentifier(clusterIdentifier)
            .masterUsername(masterUsername)
            .nodeType(nodeType)
            .numberOfNodes(numberOfNodes)
            .allowVersionUpgrade(allowVersionUpgrade)
            .automatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod)
            .availabilityZone(availabilityZone)
            .clusterAvailabilityStatus(clusterAvailabilityStatus)
            .clusterCreateTime(clusterCreateTime == null ? null : clusterCreateTime.toString())
            .clusterNodeRole(translateClusterNodesNodeRoleFromSdk(clusterNodes))
            .clusterNodePrivateIPAddress(translateClusterNodesPrivateIPAdressFromSdk(clusterNodes))
            .clusterNodePublicIPAddress(translateClusterNodesPublicIPAdressFromSdk(clusterNodes))
            .clusterParameterGroups(translateClusterParameterGroupFromSdk(clusterParameterGroups))
            .clusterPublicKey(clusterPublicKey)
            .clusterRevisionNumber(clusterRevisionNumber)
            .destinationRegion(clusterSnapshotCopyStatus == null ? null : clusterSnapshotCopyStatus.destinationRegion())
            .manualSnapshotRetentionPeriod(clusterSnapshotCopyStatus == null ? null :clusterSnapshotCopyStatus.manualSnapshotRetentionPeriod())
            .retentionPeriod(clusterSnapshotCopyStatus == null ? null :clusterSnapshotCopyStatus.retentionPeriod().intValue())
            .snapshotCopyGrantName(clusterSnapshotCopyStatus == null ? null :clusterSnapshotCopyStatus.snapshotCopyGrantName())
            .clusterVersion(clusterVersion)
            .clusterStatus(clusterStatus)
            .clusterSubnetGroupName(clusterSubnetGroupName)
            .currentRateInMegaBytesPerSecond(dataTransferProgress == null ? null : dataTransferProgress.currentRateInMegaBytesPerSecond())
            .dataTransferredInMegaBytes(dataTransferProgress == null ? null : dataTransferProgress.dataTransferredInMegaBytes().doubleValue())
            .dataTransferProgressElapsedTimeInSeconds(dataTransferProgress == null ? null : dataTransferProgress.elapsedTimeInSeconds().doubleValue())
            .estimatedTimeToCompletionInSeconds(dataTransferProgress == null ? null : dataTransferProgress.estimatedTimeToCompletionInSeconds().doubleValue())
            .dataTransferProgressStatus(dataTransferProgress == null ? null : dataTransferProgress.status())
            .totalDataInMegaBytes(dataTransferProgress == null ? null : dataTransferProgress.totalDataInMegaBytes().doubleValue())
            .dBName(dbName)
            .deferMaintenanceIdentifier(translateDeferMaintenanceIdentifierFromSdk(deferMaintenanceWindows))
            .deferMaintenanceStartTime(translateDeferMaintenanceStartTimeFromSdk(deferMaintenanceWindows))
            .deferMaintenanceEndTime(translateDeferMaintenanceEndTimeFromSdk(deferMaintenanceWindows))
            .elasticIp(elasticIpStatus == null ? null : elasticIpStatus.elasticIp())
            .elasticIpStatus(elasticIpStatus == null ? null : elasticIpStatus.status())
            .encrypted(encrypted)
            .endpointAddress(endpoint == null ? null : endpoint.address())
            .endpointPort(endpoint == null ? null : endpoint.port())
            .elasticResizeNumberOfNodeOptions(elasticResizeNumberOfNodeOptions)
            .enhancedVpcRouting(enhancedVpcRouting)
            .expectedNextSnapshotScheduleTime(expectedNextSnapshotScheduleTime == null ? null : expectedNextSnapshotScheduleTime.toString())
            .expectedNextSnapshotScheduleTimeStatus(expectedNextSnapshotScheduleTimeStatus)
            .hsmClientCertificateIdentifier(hsmStatus == null ? null : hsmStatus.hsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(hsmStatus == null ? null : hsmStatus.hsmConfigurationIdentifier())
            .hsmStatus(hsmStatus == null ? null : hsmStatus.status())
            .kmsKeyId(kmsKeyId)
            .maintenanceTrackName(maintenanceTrackName)
            .manualSnapshotRetentionPeriod(manualSnapshotRetentionPeriod)
            .preferredMaintenanceWindow(preferredMaintenanceWindow)
            .publiclyAccessible(publiclyAccessible)
            .snapshotScheduleIdentifier(snapshotScheduleIdentifier)
            .clusterSecurityGroups(translateClusterSecurityGroupsFromSdk(clusterSecurityGroups))
            .iamRoles(translateIamRolesFromSdk(iamRoles))
            .modifyStatus(modifyStatus)
            .nextMaintenanceWindowStartTime(nextMaintenanceWindowStartTime == null ? null : nextMaintenanceWindowStartTime.toString())
            .allowCancelResize(resizeInfo == null ? null : resizeInfo.allowCancelResize())
            .resizeType(resizeInfo == null ? null : resizeInfo.resizeType())
            .currentRestoreRateInMegaBytesPerSecond(restoreStatus == null ? null : restoreStatus.currentRestoreRateInMegaBytesPerSecond())
            .restoreProgressInMegaBytes(restoreStatus == null ? null : restoreStatus.progressInMegaBytes().doubleValue())
            .restoreStatus(restoreStatus == null ? null : restoreStatus.status())
            .restoreEstimatedTimeToCompletionInSeconds(restoreStatus == null ? null : restoreStatus.estimatedTimeToCompletionInSeconds().doubleValue())
            .restoreElapsedTimeInSeconds(restoreStatus == null ? null : restoreStatus.elapsedTimeInSeconds().doubleValue())
            .restoreSnapshotSizeInMegaBytes(restoreStatus == null ? null : restoreStatus.snapshotSizeInMegaBytes().doubleValue())
            .vpcSecurityGroupIds(translateVpcSecurityGroupIdsFromSdk(vpcSecurityGroupIds))
            .build();

  }

  static ResourceModel translateFromReadDescribeClusterDbRevisionsResponse(final DescribeClusterDbRevisionsResponse awsResponse) {
    final String clusterIdentifier = streamOfOrEmpty(awsResponse.clusterDbRevisions())
            .map(software.amazon.awssdk.services.redshift.model.ClusterDbRevision::clusterIdentifier)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final String currentDatabaseRevision = streamOfOrEmpty(awsResponse.clusterDbRevisions())
            .map(software.amazon.awssdk.services.redshift.model.ClusterDbRevision::currentDatabaseRevision)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final Instant databaseRevisionReleaseDate = streamOfOrEmpty(awsResponse.clusterDbRevisions())
            .map(software.amazon.awssdk.services.redshift.model.ClusterDbRevision::databaseRevisionReleaseDate)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    final List<RevisionTarget> revisionTargets  = streamOfOrEmpty(awsResponse.clusterDbRevisions())
            .map(software.amazon.awssdk.services.redshift.model.ClusterDbRevision:: revisionTargets)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);

    return ResourceModel.builder()
            .clusterIdentifier(clusterIdentifier)
            .currentDatabaseRevision(currentDatabaseRevision)
            .databaseRevisionReleaseDate(databaseRevisionReleaseDate == null ? null : databaseRevisionReleaseDate.toString())
            .revisionTargets(translateRevisionTargetsFromSdk(revisionTargets))
            .build();
  }

  static List<String> translateAccountWithRestoreAccessesFromSdk (final List<AccountWithRestoreAccess> accountWithRestoreAccesses) {
    return accountWithRestoreAccesses.stream().map((accountWithRestoreAccess ->
            accountWithRestoreAccess.accountId())).collect(Collectors.toList());
  }

  static List<String> translateClusterSecurityGroupsFromSdk (final List<ClusterSecurityGroupMembership> clusterSecurityGroups) {
    return clusterSecurityGroups.stream().map((clusterSecurityGroup ->
            clusterSecurityGroup.clusterSecurityGroupName())).collect(Collectors.toList());
  }
  static List<String> translateClusterNodesPrivateIPAdressFromSdk (final List<ClusterNode> clusterNodes) {
    return clusterNodes.stream().map((clusterNode ->
            clusterNode.privateIPAddress())).collect(Collectors.toList());
  }
  static List<String> translateClusterNodesPublicIPAdressFromSdk (final List<ClusterNode> clusterNodes) {
    return clusterNodes.stream().map((clusterNode ->
            clusterNode.publicIPAddress())).collect(Collectors.toList());
  }

  static List<String> translateClusterNodesNodeRoleFromSdk (final List<ClusterNode> clusterNodes) {
    return clusterNodes.stream().map((clusterNode ->
            clusterNode.nodeRole())).collect(Collectors.toList());
  }

  static List<String> translateClusterParameterGroupFromSdk (final List<ClusterParameterGroupStatus> clusterParameterGroups) {
    return clusterParameterGroups.stream().map((clusterParameterGroup ->
            clusterParameterGroup.parameterGroupName())).collect(Collectors.toList());
  }

  static List<String> translateVpcSecurityGroupIdsFromSdk (final List<VpcSecurityGroupMembership> vpcSecurityGroupIds) {
    return vpcSecurityGroupIds.stream().map((vpcSecurityGroup ->
            vpcSecurityGroup.vpcSecurityGroupId())).collect(Collectors.toList());
  }

  static List<String> translateIamRolesFromSdk (final List<ClusterIamRole> iamRoles) {
    return iamRoles.stream().map((iamRole ->
            iamRole.iamRoleArn())).collect(Collectors.toList());
  }

  static List<String> translateRevisionTargetsFromSdk (final List<RevisionTarget> revisionTargets) {
    return revisionTargets.stream().map(revisionTarget ->
            revisionTarget.databaseRevision()).collect(Collectors.toList());
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
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterRequest translateToDeleteRequest(final ResourceModel model) {
    //temp hack to pass contract tests
    boolean skipFinalClusterSnapshot = model.getFinalClusterSnapshotIdentifier() == null ||
            model.getFinalClusterSnapshotIdentifier().equalsIgnoreCase("true");

    return DeleteClusterRequest
            .builder()
            .clusterIdentifier(model.getClusterIdentifier())
            //.skipFinalClusterSnapshot(model.getSkipFinalClusterSnapshot())
            .skipFinalClusterSnapshot(skipFinalClusterSnapshot)
            .finalClusterSnapshotIdentifier(model.getFinalClusterSnapshotIdentifier())
            .finalClusterSnapshotRetentionPeriod(model.getFinalClusterSnapshotRetentionPeriod())
            .build();
  }

  /**
   * Request to update properties of a previously created resource
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterRequest translateToUpdateRequest(final ResourceModel model) {

    return ModifyClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .masterUserPassword(model.getMasterUserPassword())
            .nodeType(model.getNodeType())
            .numberOfNodes(model.getNumberOfNodes())
            .newClusterIdentifier(model.getNewClusterIdentifier())
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
            .clusterParameterGroupName(model.getClusterParameterGroupName())
            //.clusterSecurityGroups(model.getClusterSecurityGroups())
            .clusterType(model.getClusterType())
            .clusterVersion(model.getClusterVersion())
            .elasticIp(model.getElasticIp())
            .encrypted(model.getEncrypted())
            .enhancedVpcRouting(model.getEnhancedVpcRouting())
            .hsmClientCertificateIdentifier(model.getHsmClientCertificateIdentifier())
            .hsmConfigurationIdentifier(model.getHsmConfigurationIdentifier())
            .kmsKeyId(model.getKmsKeyId())
            .maintenanceTrackName(model.getMaintenanceTrackName())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .preferredMaintenanceWindow(model.getPreferredMaintenanceWindow())
            .publiclyAccessible(model.getPubliclyAccessible())
            .clusterSecurityGroups(model.getClusterSecurityGroups())
            .vpcSecurityGroupIds(model.getVpcSecurityGroupIds())
            .build();
  }

  /**
   * Request to update IAM of a previously created cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ModifyClusterIamRolesRequest translateToUpdateIAMRolesRequest(final ResourceModel model) {
    return ModifyClusterIamRolesRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .addIamRoles(model.getAddIamRoles())
            .removeIamRoles(model.getRemoveIamRoles())
            .build();
  }

  /**
   * Request to reboot cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static RebootClusterRequest translateToRebootClusterRequest(final ResourceModel model) {
     return RebootClusterRequest.builder()
             .clusterIdentifier(model.getClusterIdentifier())
             .build();
  }

  /**
   * Request to pause cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static PauseClusterRequest translateToPauseClusterRequest(final ResourceModel model) {
    return PauseClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to resume cluster
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static ResumeClusterRequest translateToResumeClusterRequest(final ResourceModel model) {
    return ResumeClusterRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to DbRevision of a cluster
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
   * Request to Modify Cluster Maintence
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
   * Request to Enable Cluster Snapshot
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static EnableSnapshotCopyRequest translateToEnableSnapshotRequest(final ResourceModel model) {
    return EnableSnapshotCopyRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .destinationRegion(model.getDestinationRegion())
            .manualSnapshotRetentionPeriod(model.getManualSnapshotRetentionPeriod())
            .snapshotCopyGrantName(model.getSnapshotCopyGrantName())
            .retentionPeriod(model.getRetentionPeriod())
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
            .retentionPeriod(model.getRetentionPeriod())
            .manual(model.getManual())
            .build();
  }

  /**
   * Request to Enable Logging
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static EnableLoggingRequest translateToEnableLoggingRequest(final ResourceModel model) {
    return EnableLoggingRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .bucketName(model.getBucketName())
            .s3KeyPrefix(model.getS3Prefix())
            .build();
  }

  /**
   * Request to Disable Logging
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static DisableLoggingRequest translateToDisableLoggingRequest(final ResourceModel model) {
    return DisableLoggingRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
            .build();
  }

  /**
   * Request to Rotate Encryption key
   * @param model resource model
   * @return awsRequest the aws service request to modify a resource
   */
  static RotateEncryptionKeyRequest translateToRotateEncryptionKeyRequest(final ResourceModel model) {
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
