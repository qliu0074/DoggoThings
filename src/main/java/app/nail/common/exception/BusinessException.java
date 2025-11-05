package app.nail.common.exception;

/** English: Raised when domain rules are violated. */
public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super(message);
    }
}
