package com.geonews.integration.api.dto;

import java.time.Instant;

public record NewsStreamEvent(String eventType, Instant emittedAt, NewsResponse data) {
}
