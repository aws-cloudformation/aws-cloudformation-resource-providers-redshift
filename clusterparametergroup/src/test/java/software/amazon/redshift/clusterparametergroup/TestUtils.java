package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestUtils {
    final static String DESCRIPTION = "description";
    final static String PARAMETER_GROUP_FAMILY = "redshift-1.0";
    final static String PARAMETER_GROUP_NAME = "myclusterparametergroup";
    final static String AWS_ACCOUNT_ID ="1111";

    final static String AWS_REGION = "us-east-1";
    final static String AWS_REGION_CN = "cn-north-1";
    final static String AWS_REGION_GOV = "us-gov-west-1";

    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");

    final static List<Tag> TAGS = Arrays.asList(new Tag("key1", "val1"), new Tag("key2", "val2"), new Tag("key3", "val3"));

    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValue").build()
    );

    final static List<TaggedResource> TAGGED_RESOURCES = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build()
    );

    final static List<Parameter> PARAMETERS = Arrays.asList(
            new Parameter("auto_analyze", "true"),
            new Parameter("datestyle", "ISO, MDY"),
            new Parameter("enable_user_activity_logging", "true")
    );

    final static List<software.amazon.awssdk.services.redshift.model.Parameter> SDK_PARAMETERS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("auto_analyze").parameterValue("true").build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("datestyle").parameterValue("ISO, MDY").build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("enable_user_activity_logging").parameterValue("true").build()
    );

    final static String ARN = String.format("arn:aws:redshift:%s:%s:parametergroup:%s", AWS_REGION, AWS_ACCOUNT_ID, PARAMETER_GROUP_NAME);

    final static ResourceModel COMPLETE_MODEL = ResourceModel.builder()
            .parameterGroupName(PARAMETER_GROUP_NAME)
            .description(DESCRIPTION)
            .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
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

    final static ClusterParameterGroup CLUSTER_PARAMETER_GROUP = ClusterParameterGroup.builder()
            .parameterGroupName(PARAMETER_GROUP_NAME)
            .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
            .description(DESCRIPTION)
            .tags(SDK_TAGS)
            .build();

    final static List<TaggedResource> TAGGED_RESOURCES_CREATING = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1_create").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValueCreated").build()).build()
    );

    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS_TO_CREATE = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValue").build()
    );

    final static List<String>  SDK_TAG_KEYS_TO_DELETE = ImmutableList.of("key3");

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE_CREATING = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES_CREATING)
            .build();

    final static CreateTagsRequest CREATE_TAGS_REQUEST = CreateTagsRequest.builder().resourceName(ARN).tags(SDK_TAGS_TO_CREATE).build();
    final static DeleteTagsRequest DELETE_TAGS_REQUEST = DeleteTagsRequest.builder().resourceName(ARN).tagKeys(SDK_TAG_KEYS_TO_DELETE).build();
}
