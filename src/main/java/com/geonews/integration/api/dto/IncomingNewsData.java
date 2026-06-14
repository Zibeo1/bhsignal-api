package com.geonews.integration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record IncomingNewsData(
        @NotBlank String source,
        @NotBlank String sourceArticleId,
        @NotBlank String title,
        @NotNull String summary,
        @NotBlank String url,
        @NotNull Instant publishedAt,
        String category,
        String author,
        String imageUrl,
        String locationTagRaw,
        String locationName,
        Double latitude,
        Double longitude,
        double locationConfidence,
        @NotBlank String precision,
        @NotNull Instant updatedAt
) {
}
