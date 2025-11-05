package app.nail.common.exception;

/** English: Raised when request data fails validation. */
public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message);
    }
}
