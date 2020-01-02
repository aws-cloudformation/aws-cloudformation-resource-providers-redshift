package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroup;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

public class TestUtils {
    final static String PARAMETER_GROUP_NAME = "name";
    final static String DESCRIPTION = "description";
    final static String PARAMETER_GROUP_FAMILY = "family";

    final static String AWS_REGION = "us-east-1";
    final static String AWS_REGION_CN = "cn-north-1";
    final static String AWS_REGION_GOV = "us-gov-west-1";

    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");
    final static List<Tag> TAGS = Arrays.asList(new Tag("key1", "val1"), new Tag("key2", "val2"), new Tag("key3", "val3"));
    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()
    );
    final static List<TaggedResource> TAGGED_RESOURCES = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build()
    );

    final static List<Parameter> PARAMETERS = Arrays.asList(
            new Parameter("param1", "pval1"),
            new Parameter("param2", "pval2"),
            new Parameter("param3", "pval3")
    );
    final static List<software.amazon.awssdk.services.redshift.model.Parameter> SDK_PARAMETERS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("param1").parameterValue("pval1").build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("param2").parameterValue("pval2").build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("param3").parameterValue("pval3").build()
    );

    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .description("description")
            .parameterGroupFamily("family")
            .build();

    final static ResourceModel COMPLETE_MODEL = ResourceModel.builder()
            .parameterGroupName("name")
            .description("description")
            .parameterGroupFamily("family")
            .tags(TAGS)
            .parameters(PARAMETERS)
            .build();

    final static ClusterParameterGroup PARAMETER_GROUP = ClusterParameterGroup.builder()
            .description(DESCRIPTION)
            .tags(SDK_TAGS)
            .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
            .parameterGroupName(PARAMETER_GROUP_NAME)
            .build();
    final static DescribeClusterParameterGroupsResponse DESCRIBE_CLUSTER_PARAMETER_GROUPS_RESPONSE = DescribeClusterParameterGroupsResponse.builder()
            .parameterGroups(PARAMETER_GROUP)
            .build();

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES)
            .build();

    final static DescribeClusterParametersResponse DESCRIBE_CLUSTER_PARAMETERS_RESPONSE = DescribeClusterParametersResponse.builder()
            .parameters(SDK_PARAMETERS)
            .build();
}
