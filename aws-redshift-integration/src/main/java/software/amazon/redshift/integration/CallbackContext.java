package software.amazon.redshift.integration;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private String integrationArn;

    // used to keep track of post-delete delay in seconds
    // used in software.amazon.rds.integration.DeleteHandler.delay
    private int deleteWaitTime;

    public CallbackContext() {
        super();
    }
}
