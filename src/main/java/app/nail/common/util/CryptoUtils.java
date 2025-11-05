package app.nail.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** English: Symmetric encryption helpers using AES-GCM. */
public final class CryptoUtils {
    private CryptoUtils() {}
    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_BITS = 128; // English: 16-byte tag

    /** English: Encrypts plaintext with AES-GCM. Caller must pass 16/32-byte key and 12-byte IV. */
    public static String encryptAesGcm(String plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ct);
    }

    /** English: Decrypts ciphertext (Base64) with AES-GCM. */
    public static String decryptAesGcm(String ciphertextB64, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
        byte[] pt = cipher.doFinal(Base64.getDecoder().decode(ciphertextB64));
        return new String(pt, StandardCharsets.UTF_8);
    }
}
