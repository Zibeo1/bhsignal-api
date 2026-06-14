package com.geonews.integration.api.dto;

import java.time.Instant;
import java.util.UUID;

public record NewsResponse(
        UUID id,
        String source,
        String sourceArticleId,
        String title,
        String summary,
        String url,
        String category,
        String author,
        String imageUrl,
        Instant publishedAt,
        String locationTagRaw,
        String locationName,
        Double latitude,
        Double longitude,
        double locationConfidence,
        String precision,
        Instant updatedAt
) {
}
