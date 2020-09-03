package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.model.Cluster;

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

    final static Cluster BASIC_CLUSTER = Cluster.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .build();

    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .clusterIdentifier(CLUSTER_IDENTIFIER)
            .masterUsername(MASTER_USERNAME)
            .nodeType(NODETYPE)
            .numberOfNodes(NUMBER_OF_NODES)
            .build();

}
