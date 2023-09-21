package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotCopyStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.lang.reflect.Field;
import java.util.Collections;

public class TestUtils {
    final static String AWS_REGION = "us-east-1";
    final static String DESCRIPTION = "description";
    final static String SUBNET_GROUP_NAME = "name";
    final static String AWS_PARTITION = "aws";
    final static String AWS_ACCOUNT_ID ="1111";
    final static String CLUSTER_IDENTIFIER = "redshift-cluster-1";
    final static String CLUSTER_IDENTIFIER_COMPLETE = "redshift-cluster-2";
    final static String MASTER_USERNAME = "master";
    final static String MASTER_USERPASSWORD = "Test1234";
    final static String NODETYPE = "ds2.xlarge";
    final static int NUMBER_OF_NODES = 2;
    final static String IAM_ROLE_ARN = "arn:aws:iam::1111:role/cfn_migration_test_IAM_role";
    final static String BUCKET_NAME = "bucket-enable-logging";
    final static String S3_KEY_PREFIX = "create";
    final static String OWNER_ACCOUNT_NO = "1111";

    final static Cluster BASIC_CLUSTER = Cluster.builder()
            .clusterStatus("available")
            .clusterAvailabilityStatus("Available")
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
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroups(Collections.emptyList())
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
            .clusterSecurityGroups(Collections.emptyList())
            .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroups(Collections.emptyList())
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
            .enhancedVpcRouting(false)
            .manualSnapshotRetentionPeriod(1)
            .publiclyAccessible(false)
            .clusterSecurityGroups(Collections.emptyList())
            .iamRoles(Collections.emptyList())
            .vpcSecurityGroupIds(Collections.emptyList())
            .tags(Collections.emptyList())
            .build();

    final static ResourceHandlerRequest<ResourceModel> BASIC_RESOURCE_HANDLER_REQUEST = ResourceHandlerRequest.<ResourceModel>builder()
            .region(AWS_REGION)
            .awsAccountId(AWS_ACCOUNT_ID)
            .awsPartition(AWS_PARTITION)
            .build();

    public static <T> void modifyAttribute(T object, Class<T> clazz, String attributeName, Object attributeValue) throws Exception {
        try {
            Field field = clazz.getDeclaredField(attributeName);
            field.setAccessible(true);
            field.set(object, attributeValue);
        } catch (NoSuchFieldException noSuchFieldException) {
            return;
        }
    }

}
