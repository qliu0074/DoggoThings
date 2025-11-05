package app.nail.common.exception;

/** English: Raised when a requested resource does not exist. */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
