package app.nail.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** English: Hash helpers for privacy-preserving matching. */
public final class HashUtils {
    private HashUtils() {}
    /** English: Hex SHA-256 for phone_hash. */
    public static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
