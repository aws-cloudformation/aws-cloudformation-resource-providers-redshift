package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;

import java.util.LinkedList;

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
    final static String BUCKET_NAME = "bucket-enable-logging";
    final static String S3_KEY_PREFIX = "create";
    final static String RESOURCE_NAME_PREFIX = "arn:aws:redshift:";
    final static String OWNER_ACCOUNT_NO = "1111";

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

    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .masterUserPassword(MASTER_USERPASSWORD)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .clusterType("multi-node")
            .allowVersionUpgrade(true)
            .automatedSnapshotRetentionPeriod(0)
            .encrypted(false)
            //.enhancedVpcRouting(false)
            //.manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(new LinkedList<String>())
            .iamRoles(new LinkedList<String>())
            .vpcSecurityGroupIds(new LinkedList<String>())
            .tags(new LinkedList<Tag>())
            .build();

}
