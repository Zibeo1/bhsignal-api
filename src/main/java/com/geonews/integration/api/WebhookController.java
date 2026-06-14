package com.geonews.integration.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geonews.integration.api.dto.IncomingWebhookEnvelope;
import com.geonews.integration.api.dto.WebhookAckResponse;
import com.geonews.integration.service.SignatureService;
import com.geonews.integration.service.WebhookIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ObjectMapper objectMapper;
    private final SignatureService signatureService;
    private final WebhookIngestionService webhookIngestionService;

    public WebhookController(
            ObjectMapper objectMapper,
            SignatureService signatureService,
            WebhookIngestionService webhookIngestionService
    ) {
        this.objectMapper = objectMapper;
        this.signatureService = signatureService;
        this.webhookIngestionService = webhookIngestionService;
    }

    @PostMapping("/news")
    public ResponseEntity<WebhookAckResponse> ingestNewsWebhook(
            @RequestHeader(name = "X-Event-Id", required = false) String eventId,
            @RequestHeader(name = "X-Signature-256", required = false) String signature,
            @Valid @RequestBody String rawPayload
    ) {
        if (eventId == null || eventId.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Missing X-Event-Id header");
        }

        try {
            signatureService.verifyOrThrow(rawPayload, signature);
        } catch (SecurityException ex) {
            throw new ResponseStatusException(FORBIDDEN, ex.getMessage(), ex);
        }

        IncomingWebhookEnvelope envelope = parseEnvelope(rawPayload);
        boolean processed = webhookIngestionService.ingest(eventId, envelope).isPresent();

        if (!processed) {
            return ResponseEntity.ok(new WebhookAckResponse("duplicate", "Event already processed"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new WebhookAckResponse("accepted", "Webhook processed successfully"));
    }

    private IncomingWebhookEnvelope parseEnvelope(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, IncomingWebhookEnvelope.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid webhook payload", ex);
        }
    }
}
