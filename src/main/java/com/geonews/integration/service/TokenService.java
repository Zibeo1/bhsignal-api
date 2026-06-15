package com.geonews.integration.service;

import com.geonews.integration.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Issues and verifies stateless, HMAC-signed bearer tokens.
 * Token format: base64url(userId|expiryEpochSeconds).hmacHex
 */
@Service
public class TokenService {

    private final AppProperties appProperties;
    private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder urlDecoder = Base64.getUrlDecoder();

    public TokenService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String issue(UUID userId) {
        long expiry = Instant.now().getEpochSecond() + appProperties.getAuth().getTokenTtlSeconds();
        String payload = userId + "|" + expiry;
        String encodedPayload = urlEncoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    /**
     * Returns the user id if the token is valid and unexpired, otherwise throws SecurityException.
     */
    public UUID verify(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityException("Missing token");
        }

        String value = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        String[] parts = value.split("\\.", 2);
        if (parts.length != 2) {
            throw new SecurityException("Malformed token");
        }

        String encodedPayload = parts[0];
        String providedSignature = parts[1];
        String expectedSignature = sign(encodedPayload);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Invalid token signature");
        }

        String payload = new String(urlDecoder.decode(encodedPayload), StandardCharsets.UTF_8);
        String[] payloadParts = payload.split("\\|", 2);
        if (payloadParts.length != 2) {
            throw new SecurityException("Malformed token payload");
        }

        long expiry;
        try {
            expiry = Long.parseLong(payloadParts[1]);
        } catch (NumberFormatException ex) {
            throw new SecurityException("Malformed token expiry");
        }

        if (Instant.now().getEpochSecond() > expiry) {
            throw new SecurityException("Token expired");
        }

        try {
            return UUID.fromString(payloadParts[0]);
        } catch (IllegalArgumentException ex) {
            throw new SecurityException("Malformed token subject");
        }
    }

    private String sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    appProperties.getAuth().getTokenSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            byte[] digest = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
            return urlEncoder.encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign token", ex);
        }
    }
}
