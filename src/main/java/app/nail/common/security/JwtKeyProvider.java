package app.nail.common.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Provides the shared JWT signing key after validating configuration safety.
 */
@Component
public class JwtKeyProvider {

    private static final String DEFAULT_SECRET = "change_this_in_env";

    private final Key key;

    public JwtKeyProvider(@Value("${app.security.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret must be configured.");
        }
        if (DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException("app.security.jwt.secret must be changed from the default value.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Key getKey() {
        return key;
    }
}
