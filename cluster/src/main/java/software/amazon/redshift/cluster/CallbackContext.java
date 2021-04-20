package software.amazon.redshift.cluster;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    LoggingProperties loggingProperties;
    int retryCount = 0;
    boolean callBackForReboot = false;

    public void setLoggingProperties(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public LoggingProperties getLoggingProperties() {
        return loggingProperties;
    }

    public void setCallBackForReboot(boolean callBackForReboot) {
        this.callBackForReboot = callBackForReboot;
    }

    public boolean getCallBackForReboot() {
        return callBackForReboot;
    }
}
