package com.geonews.integration.service;

import com.geonews.integration.api.dto.IncomingNewsData;
import com.geonews.integration.api.dto.IncomingWebhookEnvelope;
import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.domain.NewsItemEntity;
import com.geonews.integration.domain.ProcessedEventEntity;
import com.geonews.integration.repository.NewsItemRepository;
import com.geonews.integration.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class WebhookIngestionService {

    private final NewsItemRepository newsItemRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final NewsMapper newsMapper;
    private final NewsStreamService newsStreamService;

    public WebhookIngestionService(
            NewsItemRepository newsItemRepository,
            ProcessedEventRepository processedEventRepository,
            NewsMapper newsMapper,
            NewsStreamService newsStreamService
    ) {
        this.newsItemRepository = newsItemRepository;
        this.processedEventRepository = processedEventRepository;
        this.newsMapper = newsMapper;
        this.newsStreamService = newsStreamService;
    }

    @Transactional
    public Optional<NewsResponse> ingest(String eventId, IncomingWebhookEnvelope envelope) {
        if (processedEventRepository.existsById(eventId)) {
            return Optional.empty();
        }

        IncomingNewsData data = envelope.data();

        NewsItemEntity entity = newsItemRepository
                .findBySourceAndSourceArticleId(data.source(), data.sourceArticleId())
                .orElseGet(NewsItemEntity::new);

        applyData(entity, data);
        entity.setIngestedAt(Instant.now());

        NewsItemEntity saved = newsItemRepository.save(entity);
        processedEventRepository.save(new ProcessedEventEntity(eventId, envelope.eventType(), Instant.now()));

        NewsResponse response = newsMapper.toResponse(saved);
        newsStreamService.broadcast(envelope.eventType(), response);
        return Optional.of(response);
    }

    private void applyData(NewsItemEntity entity, IncomingNewsData data) {
        entity.setSource(data.source());
        entity.setSourceArticleId(data.sourceArticleId());
        entity.setTitle(data.title());
        entity.setSummary(data.summary());
        entity.setUrl(data.url());
        entity.setCategory(data.category());
        entity.setAuthor(data.author());
        entity.setImageUrl(data.imageUrl());
        entity.setPublishedAt(data.publishedAt());
        entity.setLocationTagRaw(data.locationTagRaw());
        entity.setLocationName(data.locationName());
        entity.setLatitude(data.latitude());
        entity.setLongitude(data.longitude());
        entity.setLocationConfidence(data.locationConfidence());
        entity.setPrecision(data.precision());
        entity.setUpdatedAt(data.updatedAt());
    }
}
