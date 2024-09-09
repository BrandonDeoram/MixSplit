package com.demo.MixSplit.Utility;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AudioUtils {
    public static byte[] audioToBytes(String absPath) {
        Path path = Paths.get(absPath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading audio file: " + absPath, e);
        }
    }

    // Generate HMAC-SHA1 signature
    public static String generateSignature(String stringToSign, String accessSecret) {
        try {
            Mac sha1Hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            sha1Hmac.init(secretKeySpec);

            byte[] hash = sha1Hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC-SHA1 signature", e);
        }
    }
}
