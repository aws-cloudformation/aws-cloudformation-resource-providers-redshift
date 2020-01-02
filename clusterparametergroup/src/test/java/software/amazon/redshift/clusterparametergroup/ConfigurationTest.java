package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class ConfigurationTest {

    @Test
    public void testResourceDefinedTagsEmptyModel() {
        final Configuration configuration = new Configuration();
        final ResourceModel model = ResourceModel.builder().build();
        assertNull(configuration.resourceDefinedTags(model));
    }

    @Test
    public void testResourceDefinedTags() {
        final Configuration configuration = new Configuration();
        final List<Tag> tags = Arrays.asList(new Tag("key", "val"));
        final ResourceModel model = ResourceModel.builder()
                .tags(tags)
                .build();
        assertEquals(ImmutableMap.of("key", "val"), configuration.resourceDefinedTags(model));
    }
}
