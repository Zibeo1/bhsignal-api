package com.geonews.integration.service;

import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.domain.NewsItemEntity;
import org.springframework.stereotype.Component;

@Component
public class NewsMapper {

    public NewsResponse toResponse(NewsItemEntity entity) {
        return new NewsResponse(
                entity.getId(),
                entity.getSource(),
                entity.getSourceArticleId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getUrl(),
                entity.getCategory(),
                entity.getAuthor(),
                entity.getImageUrl(),
                entity.getPublishedAt(),
                entity.getLocationTagRaw(),
                entity.getLocationName(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getLocationConfidence(),
                entity.getPrecision(),
                entity.getUpdatedAt()
        );
    }
}
