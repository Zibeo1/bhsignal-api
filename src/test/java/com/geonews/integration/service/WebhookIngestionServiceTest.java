package com.geonews.integration.service;

import com.geonews.integration.api.dto.IncomingNewsData;
import com.geonews.integration.api.dto.IncomingWebhookEnvelope;
import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.domain.NewsItemEntity;
import com.geonews.integration.domain.ProcessedEventEntity;
import com.geonews.integration.repository.NewsItemRepository;
import com.geonews.integration.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookIngestionServiceTest {

    @Mock
    private NewsItemRepository newsItemRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private NewsStreamService newsStreamService;

    @InjectMocks
    private WebhookIngestionService webhookIngestionService;

    private IncomingWebhookEnvelope envelope;
    private NewsItemEntity savedEntity;
    private NewsResponse mappedResponse;

    @BeforeEach
    void setUp() {
        IncomingNewsData data = new IncomingNewsData(
                "klix", "klix-001", "Test Title", "Test summary",
                "https://example.com", Instant.now(), "POLITICAL", "Author",
                null, "Sarajevo", "Sarajevo", 43.85, 18.41, 0.95, "city", Instant.now()
        );
        envelope = new IncomingWebhookEnvelope("news.created", Instant.now(), "klix", data);

        savedEntity = new NewsItemEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setSource("klix");
        savedEntity.setSourceArticleId("klix-001");
        savedEntity.setTitle("Test Title");
        savedEntity.setSummary("Test summary");
        savedEntity.setUrl("https://example.com");
        savedEntity.setLocationConfidence(0.95);
        savedEntity.setPrecision("city");
        savedEntity.setPublishedAt(Instant.now());
        savedEntity.setUpdatedAt(Instant.now());
        savedEntity.setIngestedAt(Instant.now());

        mappedResponse = new NewsResponse(
                savedEntity.getId(), "klix", "klix-001", "Test Title", "Test summary",
                "https://example.com", "POLITICAL", "Author", null,
                savedEntity.getPublishedAt(), null, "Sarajevo", 43.85, 18.41,
                0.95, "city", savedEntity.getUpdatedAt()
        );
    }

    @Test
    void ingest_withNewEvent_savesEntityAndReturnsResponse() {
        when(processedEventRepository.existsById("evt-001")).thenReturn(false);
        when(newsItemRepository.findBySourceAndSourceArticleId("klix", "klix-001"))
                .thenReturn(Optional.empty());
        when(newsItemRepository.save(any(NewsItemEntity.class))).thenReturn(savedEntity);
        when(processedEventRepository.save(any(ProcessedEventEntity.class)))
                .thenReturn(new ProcessedEventEntity("evt-001", "news.created", Instant.now()));
        when(newsMapper.toResponse(savedEntity)).thenReturn(mappedResponse);

        Optional<NewsResponse> result = webhookIngestionService.ingest("evt-001", envelope);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Test Title");
        verify(newsStreamService).broadcast(eq("news.created"), any(NewsResponse.class));
    }

    @Test
    void ingest_withDuplicateEventId_returnsEmpty() {
        when(processedEventRepository.existsById("evt-dup")).thenReturn(true);

        Optional<NewsResponse> result = webhookIngestionService.ingest("evt-dup", envelope);

        assertThat(result).isEmpty();
        verify(newsItemRepository, never()).save(any());
        verify(newsStreamService, never()).broadcast(any(), any());
    }

    @Test
    void ingest_withExistingArticle_updatesInsteadOfCreating() {
        when(processedEventRepository.existsById("evt-002")).thenReturn(false);
        when(newsItemRepository.findBySourceAndSourceArticleId("klix", "klix-001"))
                .thenReturn(Optional.of(savedEntity));
        when(newsItemRepository.save(savedEntity)).thenReturn(savedEntity);
        when(processedEventRepository.save(any())).thenReturn(null);
        when(newsMapper.toResponse(savedEntity)).thenReturn(mappedResponse);

        Optional<NewsResponse> result = webhookIngestionService.ingest("evt-002", envelope);

        assertThat(result).isPresent();
        verify(newsItemRepository, times(1)).save(savedEntity);
    }

    @Test
    void ingest_withNewEvent_broadcastsToStream() {
        when(processedEventRepository.existsById("evt-003")).thenReturn(false);
        when(newsItemRepository.findBySourceAndSourceArticleId(any(), any()))
                .thenReturn(Optional.empty());
        when(newsItemRepository.save(any())).thenReturn(savedEntity);
        when(processedEventRepository.save(any())).thenReturn(null);
        when(newsMapper.toResponse(savedEntity)).thenReturn(mappedResponse);

        webhookIngestionService.ingest("evt-003", envelope);

        verify(newsStreamService).broadcast("news.created", mappedResponse);
    }
}
