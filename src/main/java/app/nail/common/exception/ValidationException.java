package app.nail.common.exception;

import org.springframework.http.HttpStatus;

/** English: Raised when request data fails validation. */
public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(ApiErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, message);
    }
}
