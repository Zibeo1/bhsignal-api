package com.geonews.integration.service;

import com.geonews.integration.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class SignatureService {

    private final AppProperties appProperties;

    public SignatureService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void verifyOrThrow(String rawPayload, String headerSignature) {
        if (headerSignature == null || headerSignature.isBlank()) {
            throw new SecurityException("Missing X-Signature-256 header");
        }

        String expected = computeSignature(rawPayload);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] incomingBytes = headerSignature.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expectedBytes, incomingBytes)) {
            throw new SecurityException("Webhook signature mismatch");
        }
    }

    private String computeSignature(String rawPayload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    appProperties.getWebhook().getSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(key);
            byte[] digest = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + toHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not verify webhook signature", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            sb.append(String.format("%02x", value));
        }
        return sb.toString();
    }
}
