package software.amazon.redshift.clustersecuritygroup;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestUtils {

    final static String DESCRIPTION = "description";
    final static String SECURITY_GROUP_NAME = "logicalid-kvw2fztz3cvh";

    final static String AWS_REGION = "us-east-1";
    final static List<Tag> TAGS = Arrays.asList(new Tag("key1", "val1"), new Tag("key2", "val2"), new Tag("key3", "val3"));

    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");

    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValue").build()
    );

    final static ResourceModel COMPLETE_MODEL = ResourceModel.builder()
            .clusterSecurityGroupName(SECURITY_GROUP_NAME)
            .description(DESCRIPTION)
            .tags(TAGS)
            .build();

    final static ClusterSecurityGroup CLUSTER_SECURITY_GROUP = ClusterSecurityGroup.builder()
            .clusterSecurityGroupName(SECURITY_GROUP_NAME)
            .description(DESCRIPTION)
            .tags(SDK_TAGS)
            .build();
}
