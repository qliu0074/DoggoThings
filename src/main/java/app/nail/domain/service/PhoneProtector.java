package app.nail.domain.service;

import app.nail.common.util.CryptoUtils;
import app.nail.common.util.HashUtils;
import app.nail.config.PhoneSecurityProperties;
import app.nail.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * English: Handles hashing, masking and encrypting phone numbers.
 */
@Component
@RequiredArgsConstructor
public class PhoneProtector {

    private static final int GCM_IV_LENGTH = 12;
    private final PhoneSecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * English: Apply protection to the given user entity (mask + hash + encrypt).
     * Passing blank input clears the stored phone data.
     */
    public void apply(User user, String phonePlaintext) {
        if (!StringUtils.hasText(phonePlaintext)) {
            user.setPhone(null);
            user.setPhoneHash(null);
            user.setPhoneEnc(null);
            return;
        }
        String normalized = normalize(phonePlaintext);
        String hash = hashNormalized(normalized);
        byte[] encrypted = encrypt(normalized);
        user.setPhone(mask(normalized));
        user.setPhoneHash(hash);
        user.setPhoneEnc(encrypted);
    }

    /** English: Compute hash for lookups using plain input. */
    public String hash(String phonePlaintext) {
        if (!StringUtils.hasText(phonePlaintext)) {
            throw new IllegalArgumentException("phonePlaintext must not be blank");
        }
        return hashNormalized(normalize(phonePlaintext));
    }

    /** English: Reveal the original phone if encryption is available. */
    public Optional<String> reveal(User user) {
        byte[] combined = user.getPhoneEnc();
        if (combined == null || combined.length <= GCM_IV_LENGTH) {
            return Optional.empty();
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipher = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, cipher, 0, cipher.length);
        try {
            String cipherBase64 = Base64.getEncoder().encodeToString(cipher);
            return Optional.of(CryptoUtils.decryptAesGcm(cipherBase64, properties.encryptionKeyBytes(), iv));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt phone number", ex);
        }
    }

    /** English: Best-effort detection whether the stored value is already masked. */
    public boolean isMasked(String value) {
        return StringUtils.hasText(value) && value.contains("*");
    }

    private String normalize(String raw) {
        String trimmed = raw.trim();
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digitsOnly)) {
            throw new IllegalArgumentException("Phone number must contain digits");
        }
        return digitsOnly;
    }

    private String hashNormalized(String normalized) {
        return HashUtils.saltedSHA256(normalized, properties.getHashSalt());
    }

    private byte[] encrypt(String normalized) {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        try {
            String cipherBase64 = CryptoUtils.encryptAesGcm(normalized, properties.encryptionKeyBytes(), iv);
            byte[] cipherBytes = Base64.getDecoder().decode(cipherBase64);
            ByteBuffer buffer = ByteBuffer.allocate(GCM_IV_LENGTH + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);
            return buffer.array();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt phone number", ex);
        }
    }

    private String mask(String normalized) {
        int len = normalized.length();
        if (len <= 3) {
            return "*".repeat(len);
        }
        if (len <= 7) {
            return normalized.substring(0, 2) + "***" + normalized.substring(len - 2);
        }
        return normalized.substring(0, 3) + "****" + normalized.substring(len - 4);
    }
}
