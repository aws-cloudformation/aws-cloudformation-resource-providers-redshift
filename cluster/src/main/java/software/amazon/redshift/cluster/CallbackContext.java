package software.amazon.redshift.cluster;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    LoggingProperties loggingProperties;
    boolean callBackForReboot = false;
    boolean callBackForDelete = false;
    boolean callBackAfterResize = false;

    public void setLoggingProperties(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public LoggingProperties getLoggingProperties() {
        return loggingProperties;
    }

    public void setCallBackForReboot(boolean callBackForReboot) {
        this.callBackForReboot = callBackForReboot;
    }

    public boolean getCallBackForReboot() { return callBackForReboot; }

    public void setCallBackForDelete(boolean callBackForDelete) {
        this.callBackForDelete = callBackForDelete;
    }

    public boolean getCallBackForDelete() {
        return callBackForDelete;
    }

    public void setCallBackAfterResize(boolean callBackAfterResize) {
        this.callBackAfterResize = callBackAfterResize;
    }

    public boolean getCallBackAfterResize() { return callBackAfterResize; }
}
