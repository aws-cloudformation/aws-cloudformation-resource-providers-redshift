package software.amazon.redshift.eventsubscription;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
public class TestUtils {
    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");
    final static Map<String, String> PREVIOUS_TAGS = ImmutableMap.of("key4", "val4", "key2", "val2");
}