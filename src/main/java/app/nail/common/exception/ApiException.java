package app.nail.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** English: API exception carrying standardized code and HTTP status. */
public class ApiException extends RuntimeException {

    private final ApiErrorCode code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public ApiException(ApiErrorCode code, HttpStatus status, String message) {
        this(code, status, message, null, null);
    }

    public ApiException(ApiErrorCode code, HttpStatus status, String message, Throwable cause) {
        this(code, status, message, null, cause);
    }

    public ApiException(ApiErrorCode code, HttpStatus status, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public static ApiException resourceNotFound(String message) {
        return new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

    public static ApiException businessViolation(String message) {
        return new ApiException(ApiErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
    }
}
