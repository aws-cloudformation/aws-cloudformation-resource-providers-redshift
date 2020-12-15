package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterDbRevision;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.DeferredMaintenanceWindow;
import software.amazon.awssdk.services.redshift.model.RevisionTarget;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

import java.time.Instant;
import java.util.LinkedList;


public class TestUtils {
    final static String AWS_REGION = "us-east-1";
    final static String AWS_ACCOUNT_ID ="1111";
    final static String CLUSTER_IDENTIFIER = "redshift-cluster-1";
    final static String SNAPSHOT_IDENTIFIER = "redshift-cluster-snapshot-1";
    final static String SOURCE_DB = "dev";
    final static String SOURCE_TABLE = "source_table";
    final static String TARGET_DB = "dev";
    final static String NEW_TABLE = "source_table_copy";
    final static String CLUSTER_IDENTIFIER_COMPLETE = "redshift-cluster-2";
    final static String MASTER_USERNAME = "master";
    final static String MASTER_USERPASSWORD = "Test1234";
    final static String NODETYPE = "ds2.xlarge";
    final static int  NUMBER_OF_NODES = 2;
    final static String IAM_ROLE_ARN = "arn:aws:iam::1111:role/cfn_migration_test_IAM_role";
    final static String CURRENT_DB_REVISION = "1";
    final static String CLUSTER_AVAILABLE = "available";
    final static String CLUSTER_PAUSED = "paused";
    final static String RESOURCE_NAME = "arn:aws:redshift:"+AWS_REGION+":"+AWS_ACCOUNT_ID+":"+CLUSTER_IDENTIFIER;
    final static String USAGE_LIMIT_ID = "usage-limit-id";

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
            .tags(new LinkedList<Tag>())
            .clusterStatus(CLUSTER_AVAILABLE)
            .build();

    final static Cluster BASIC_CLUSTER_READ = Cluster.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterStatus(CLUSTER_AVAILABLE)
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
            .iamRoles(new LinkedList<ClusterIamRole>())
            .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
            .tags(new LinkedList<Tag>())
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
            .tags(new LinkedList<Tag>())
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

    final static DeferredMaintenanceWindow DEFERRED_MAINTENANCE_WINDOW = DeferredMaintenanceWindow.builder()
            .deferMaintenanceEndTime(Instant.now().plusSeconds(3600))
            .deferMaintenanceStartTime(Instant.now().minusSeconds(3600))
            .deferMaintenanceIdentifier("deferredMaintenanceIdentifier")
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
            .clusterParameterGroups(new LinkedList<String>())
            .clusterNodeRole(new LinkedList<String>())
            .clusterNodePrivateIPAddress(new LinkedList<String>())
            .clusterNodePublicIPAddress(new LinkedList<String>())
            .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
            .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
            .build();

    final static ResourceModel DESCRIBE_DB_REVISIONS_MODEL = ResourceModel.builder()
            .redshiftCommand("describe-cluster-db-revisions")
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .currentDatabaseRevision("0")
            .databaseRevisionReleaseDate(DB_RELEASE_DATE.toString())
            .revisionTargets(null)
            .build();

    final static ResourceModel DESCRIBE_RESIZE_MODEL = ResourceModel.builder()
            .redshiftCommand("describe-resize")
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .build();

}
