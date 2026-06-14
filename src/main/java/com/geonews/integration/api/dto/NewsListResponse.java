package com.geonews.integration.api.dto;

import java.util.List;

public record NewsListResponse(List<NewsResponse> items, String nextSince) {
}
