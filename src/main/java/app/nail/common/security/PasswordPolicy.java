package app.nail.common.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Enforces password complexity requirements to mitigate weak credential usage.
 */
@Component
public class PasswordPolicy {

    private static final int MIN_LENGTH = 12;
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Password must be provided.");
        }
        if (password.length() < MIN_LENGTH
            || !UPPER.matcher(password).find()
            || !LOWER.matcher(password).find()
            || !DIGIT.matcher(password).find()
            || !SPECIAL.matcher(password).find()) {
            throw new IllegalStateException("Password must be at least 12 characters and include upper, lower, digit, and special characters.");
        }
    }
}
