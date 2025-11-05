package app.nail.common.exception;

/** English: Simple API exception with HTTP-friendly message. */
public class ApiException extends RuntimeException {
    public ApiException(String message) { super(message); }
}
