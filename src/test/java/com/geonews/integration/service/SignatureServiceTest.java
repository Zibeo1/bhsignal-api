package com.geonews.integration.service;

import com.geonews.integration.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignatureServiceTest {

    private SignatureService signatureService;
    private static final String SECRET = "test-secret";
    private static final String PAYLOAD = "{\"eventType\":\"news.created\"}";

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getWebhook().setSecret(SECRET);
        signatureService = new SignatureService(props);
    }

    @Test
    void verifyOrThrow_withValidSignature_doesNotThrow() {
        String validSignature = computeExpectedSignature(PAYLOAD, SECRET);

        assertThatCode(() -> signatureService.verifyOrThrow(PAYLOAD, validSignature))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyOrThrow_withNullSignature_throwsSecurityException() {
        assertThatThrownBy(() -> signatureService.verifyOrThrow(PAYLOAD, null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Missing X-Signature-256");
    }

    @Test
    void verifyOrThrow_withBlankSignature_throwsSecurityException() {
        assertThatThrownBy(() -> signatureService.verifyOrThrow(PAYLOAD, "   "))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Missing X-Signature-256");
    }

    @Test
    void verifyOrThrow_withWrongSignature_throwsSecurityException() {
        assertThatThrownBy(() -> signatureService.verifyOrThrow(PAYLOAD, "sha256=wrongvalue"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("signature mismatch");
    }

    @Test
    void verifyOrThrow_withTamperedPayload_throwsSecurityException() {
        String validSignature = computeExpectedSignature(PAYLOAD, SECRET);
        String tamperedPayload = PAYLOAD + " tampered";

        assertThatThrownBy(() -> signatureService.verifyOrThrow(tamperedPayload, validSignature))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void verifyOrThrow_withWrongSecret_throwsSecurityException() {
        String signatureWithWrongSecret = computeExpectedSignature(PAYLOAD, "wrong-secret");

        assertThatThrownBy(() -> signatureService.verifyOrThrow(PAYLOAD, signatureWithWrongSecret))
                .isInstanceOf(SecurityException.class);
    }

    private String computeExpectedSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("sha256=");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
