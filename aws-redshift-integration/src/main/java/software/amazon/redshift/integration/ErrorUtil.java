package software.amazon.redshift.integration;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.model.IntegrationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.IntegrationConflictOperationException;
import software.amazon.awssdk.services.redshift.model.IntegrationConflictStateException;
import software.amazon.awssdk.services.redshift.model.IntegrationNotFoundException;
import software.amazon.awssdk.services.redshift.model.IntegrationQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.IntegrationSourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.IntegrationTargetNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedPartnerIntegrationException;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.Map;

public class ErrorUtil {

    private enum ErrorCode {
        AccessDenied("AccessDenied", HandlerErrorCode.AccessDenied),
        AccessDeniedException("AccessDeniedException", HandlerErrorCode.AccessDenied),
        NotAuthorized("NotAuthorized", HandlerErrorCode.AccessDenied),
        UnauthorizedOperation("UnauthorizedOperation", HandlerErrorCode.AccessDenied);

        private final String code;
        private final HandlerErrorCode targetCfnErrorCode;

        ErrorCode(final String code, final HandlerErrorCode targetCfnErrorCode) {
            this.code = code;
            this.targetCfnErrorCode = targetCfnErrorCode;
        }

        public static ErrorCode fromString(final String errorStr) {
            if (StringUtils.isNotBlank(errorStr)) {
                for (final ErrorCode errorCode : ErrorCode.values()) {
                    if (errorCode.equals(errorStr)) {
                        return errorCode;
                    }
                }
            }
            return null;
        }

        public static ErrorCode getErrorCodeFromException(final AwsServiceException exception) {
            final AwsErrorDetails errorDetails = exception.awsErrorDetails();
            if (errorDetails != null) {
                return ErrorCode.fromString(errorDetails.errorCode());
            }
            return null;
        }

        public static HandlerErrorCode getCfnHandlerErrorCodeFromException(final AwsServiceException exception) {
            ErrorCode errorCode = getErrorCodeFromException(exception);
            if (errorCode != null) {
                return errorCode.targetCfnErrorCode;
            }
            return null;
        }

        @Override
        public String toString() {
            return code;
        }

        public boolean equals(final String cmp) {
            return code.equals(cmp);
        }
    }

    private static Map<Class<? extends Exception>, HandlerErrorCode> INTEGRATION_EXCEPTION_TO_HANLDER_ERRORCODE =
            ImmutableMap.of(
                    IntegrationAlreadyExistsException.class, HandlerErrorCode.AlreadyExists,
                    IntegrationConflictOperationException.class, HandlerErrorCode.AlreadyExists,
                    IntegrationConflictStateException.class, HandlerErrorCode.AlreadyExists,
                    IntegrationQuotaExceededException.class, HandlerErrorCode.ServiceLimitExceeded,
                    IntegrationNotFoundException.class, HandlerErrorCode.NotFound,
                    IntegrationTargetNotFoundException.class, HandlerErrorCode.NotFound,
                    IntegrationSourceNotFoundException.class, HandlerErrorCode.NotFound,
                    UnauthorizedPartnerIntegrationException.class, HandlerErrorCode.AccessDenied
            );

    protected static ProgressEvent<ResourceModel, CallbackContext> handleIntegrationException(final Exception exception) {
        if (exception instanceof AwsServiceException) {
            HandlerErrorCode generalHandlerErrorCode = ErrorCode.getCfnHandlerErrorCodeFromException((AwsServiceException) exception);
            if (generalHandlerErrorCode != null) {
                return ProgressEvent.defaultFailureHandler(exception, generalHandlerErrorCode);
            }
        }

        HandlerErrorCode handlerErrorCode = INTEGRATION_EXCEPTION_TO_HANLDER_ERRORCODE.getOrDefault(
                exception.getClass(),
                HandlerErrorCode.GeneralServiceException);
        return ProgressEvent.defaultFailureHandler(exception, handlerErrorCode);
    }



}
