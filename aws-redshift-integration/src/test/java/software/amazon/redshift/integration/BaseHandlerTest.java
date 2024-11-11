package software.amazon.redshift.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BaseHandlerTest extends AbstractTestBase {
    @Test
    public void isCrossAccountIntegration() {
        ResourceModel model = ResourceModel.builder()
                .sourceArn("arn:aws-cn:dynamodb:us-west-2:123123123123:table/test")
                .targetArn("arn:aws-cn:redshift:us-west-2:897123123123:namespace:1231224dsfasdfwer")
                .build();
        assertTrue(BaseHandlerStd.isCrossAccountIntegration(model));
        ResourceModel sameAccountModel = ResourceModel.builder()
                .sourceArn("arn:aws-cn:dynamodb:us-west-2:123123123123:table/test")
                .targetArn("arn:aws-cn:redshift:us-west-2:123123123123:namespace:1231224dsfasdfwer")
                .build();
        assertFalse(BaseHandlerStd.isCrossAccountIntegration(sameAccountModel));
    }
}
