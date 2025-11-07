package app.nail.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** English: Converts exceptions to JSON problem response with standardized codes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        ErrorResponse body = new ErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                ex.getStatus(),
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, java.util.List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        ErrorResponse body = new ErrorResponse(
                ApiErrorCode.VALIDATION_FAILED,
                "Parameter validation failed",
                HttpStatus.BAD_REQUEST,
                Map.of("fieldErrors", fieldErrors)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        ErrorResponse body = new ErrorResponse(
                ApiErrorCode.VALIDATION_FAILED,
                "Constraint violation",
                HttpStatus.BAD_REQUEST,
                Map.of("violations", errors)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse body = new ErrorResponse(
                ApiErrorCode.VALIDATION_FAILED,
                "Invalid parameter type",
                HttpStatus.BAD_REQUEST,
                Map.of("parameter", ex.getName(), "value", ex.getValue())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                ApiErrorCode.INTERNAL_ERROR,
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                Map.of("reason", ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /** English: Standard error DTO. */
    public static final class ErrorResponse {
        private final String timestamp = OffsetDateTime.now().toString();
        private final ApiErrorCode code;
        private final String message;
        private final int status;
        private final Map<String, Object> details;

        public ErrorResponse(ApiErrorCode code, String message, HttpStatus status, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.status = status.value();
            this.details = details;
        }

        public String getTimestamp() { return timestamp; }
        public ApiErrorCode getCode() { return code; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public Map<String, Object> getDetails() { return details; }
    }
}
