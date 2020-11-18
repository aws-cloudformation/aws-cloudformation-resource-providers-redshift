package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterDbRevision;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.RevisionTarget;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
    final static String AWS_REGION = "us-east-1";
    final static String DESCRIPTION = "description";
    final static String SUBNET_GROUP_NAME = "name";
    final static String AWS_ACCOUNT_ID ="1111";
    final static String CLUSTER_IDENTIFIER = "redshift-cluster-1";
    final static String CLUSTER_IDENTIFIER_COMPLETE = "redshift-cluster-2";
    final static String MASTER_USERNAME = "master";
    final static String MASTER_USERPASSWORD = "Test1234";
    final static String NODETYPE = "ds2.xlarge";
    final static int  NUMBER_OF_NODES = 2;
    final static String IAM_ROLE_ARN = "arn:aws:iam::1111:role/cfn_migration_test_IAM_role";

    final static Cluster BASIC_CLUSTER = Cluster.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
            .iamRoles(new LinkedList<ClusterIamRole>())
            .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
            .build();

    final static Cluster BASIC_CLUSTER_READ = Cluster.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterStatus("available")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
            .iamRoles(new LinkedList<ClusterIamRole>())
            .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
            .build();

    final static Cluster BASIC_CLUSTER_READ_AFTER_DELETE = Cluster.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterStatus("unavailable")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
            .iamRoles(new LinkedList<ClusterIamRole>())
            .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
            .build();

    final static Instant DB_RELEASE_DATE = Instant.ofEpochSecond(1000000000);
    static RevisionTarget REVISION_TARGET = RevisionTarget.builder()
            .databaseRevision("")
            .databaseRevisionReleaseDate(DB_RELEASE_DATE)
            .description("This is version:1")
            .build();

    final static ClusterDbRevision CLUSTER_DB_REVISION = ClusterDbRevision.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .currentDatabaseRevision("0")
            .databaseRevisionReleaseDate(DB_RELEASE_DATE)
            .revisionTargets(REVISION_TARGET)
            .build();

    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .masterUserPassword(MASTER_USERPASSWORD)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<String>())
            .iamRoles(new LinkedList<String>())
            .vpcSecurityGroupIds(new LinkedList<String>())
            .build();

    final static ResourceModel DESCRIBE_DB_REVISIONS_MODEL = ResourceModel.builder()
            .redshiftCommand("describe-cluster-db-revisions")
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .currentDatabaseRevision("0")
            .databaseRevisionReleaseDate(DB_RELEASE_DATE.toString())
            .revisionTargets(null)
            .build();

}
