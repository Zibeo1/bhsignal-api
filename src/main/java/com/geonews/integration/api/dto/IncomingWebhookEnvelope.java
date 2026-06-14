package com.geonews.integration.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record IncomingWebhookEnvelope(
        @NotBlank String eventType,
        @NotNull Instant occurredAt,
        @NotBlank String source,
        @Valid @NotNull IncomingNewsData data
) {
}
