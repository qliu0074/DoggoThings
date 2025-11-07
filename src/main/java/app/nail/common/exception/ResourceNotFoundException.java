package app.nail.common.exception;

import org.springframework.http.HttpStatus;

/** English: Raised when a requested resource does not exist. */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(ApiErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }
}
