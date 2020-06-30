package net.runelite.client.plugins.socket.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Handles one way encryption using the SHA-256 unary directional method.
 *
 * @author https://www.baeldung.com/sha-256-hashing-java
 */
public class SHA256 {

    /**
     * Encrypts a given String using SHA-256 and returns the Base64 conversion output.
     *
     * @param origin The original string
     * @return Base64 SHA-256 encrypted string
     */
    public static String encrypt(String origin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] originBytes = origin.getBytes(StandardCharsets.UTF_8);
            byte[] encodedBinary = digest.digest(originBytes);
            return new String(Base64.getEncoder().encode(encodedBinary));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return origin;
        }
    }
}
