package app.nail.common.exception;

import org.springframework.http.HttpStatus;

/** English: Raised when domain rules are violated. */
public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super(ApiErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.BAD_REQUEST, message);
    }
}
