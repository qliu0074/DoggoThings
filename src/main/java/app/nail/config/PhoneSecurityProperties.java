package app.nail.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Base64;

/**
 * English: Configuration for protecting phone numbers.
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.security.phone")
public class PhoneSecurityProperties {

    /** English: Global salt used for hashing phone numbers. */
    @NotBlank
    private String hashSalt;

    /** English: Base64 encoded AES key (16 or 32 bytes). */
    @NotBlank
    private String encryptionKey;

    public byte[] encryptionKeyBytes() {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.security.phone.encryption-key must be valid Base64", ex);
        }
        if (decoded.length != 16 && decoded.length != 32) {
            throw new IllegalStateException("app.security.phone.encryption-key must decode to 16 or 32 bytes");
        }
        return decoded;
    }
}
