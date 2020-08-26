package software.amazon.redshift.cluster;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;


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
            .nodeType(model.getNodeType())
            .numberOfNodes(model.getNumberOfNodes())
            .additionalInfo(model.getAdditionalInfo())
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeClustersRequest translateToReadRequest(final ResourceModel model) {
    return DescribeClustersRequest.builder()
            .clusterIdentifier(model.getClusterIdentifier())
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
            .orElse(1);

    return ResourceModel.builder()
            .clusterIdentifier(clusterIdentifier)
            .masterUsername(masterUsername)
            .nodeType(nodeType)
            .numberOfNodes(numberOfNodes)
            .allowVersionUpgrade(allowVersionUpgrade)
            .automatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod)
            .build();

  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteClusterRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteClusterRequest
            .builder()
            .clusterIdentifier(model.getClusterIdentifier())
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
            .allowVersionUpgrade(model.getAllowVersionUpgrade())
            .automatedSnapshotRetentionPeriod(model.getAutomatedSnapshotRetentionPeriod())
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
