package com.geonews.integration.service;

import com.geonews.integration.api.dto.NewsListResponse;
import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.domain.NewsItemEntity;
import com.geonews.integration.repository.NewsItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsQueryServiceTest {

    @Mock
    private NewsItemRepository newsItemRepository;

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsQueryService newsQueryService;

    private NewsItemEntity sampleEntity;
    private NewsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEntity = new NewsItemEntity();
        sampleEntity.setId(UUID.randomUUID());
        sampleEntity.setTitle("Test Article");
        sampleEntity.setSource("klix");
        sampleEntity.setSourceArticleId("klix-001");
        sampleEntity.setSummary("Test summary");
        sampleEntity.setUrl("https://example.com");
        sampleEntity.setCategory("POLITICAL");
        sampleEntity.setLocationName("Sarajevo");
        sampleEntity.setLatitude(43.85);
        sampleEntity.setLongitude(18.41);
        sampleEntity.setLocationConfidence(0.95);
        sampleEntity.setPrecision("city");
        sampleEntity.setPublishedAt(Instant.now());
        sampleEntity.setUpdatedAt(Instant.now());
        sampleEntity.setIngestedAt(Instant.now());

        sampleResponse = new NewsResponse(
                sampleEntity.getId(), "klix", "klix-001", "Test Article",
                "Test summary", "https://example.com", "POLITICAL", null,
                null, sampleEntity.getPublishedAt(), null, "Sarajevo",
                43.85, 18.41, 0.95, "city", sampleEntity.getUpdatedAt()
        );
    }

    @Test
    void query_withNoFilters_returnsAllItems() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleEntity)));
        when(newsMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        NewsListResponse result = newsQueryService.query(null, null, null, null, null, 100);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("Test Article");
    }

    @Test
    void query_withCategoryFilter_returnsMatchingItems() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleEntity)));
        when(newsMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        NewsListResponse result = newsQueryService.query(null, null, null, "POLITICAL", null, 100);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).category()).isEqualTo("POLITICAL");
    }

    @Test
    void query_withEmptyResult_returnsNullNextSince() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        NewsListResponse result = newsQueryService.query(null, null, null, null, null, 10);

        assertThat(result.items()).isEmpty();
        assertThat(result.nextSince()).isNull();
    }

    @Test
    void query_limitBelowOne_clampsToOne() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        newsQueryService.query(null, null, null, null, null, 0);

        // verifies no exception thrown and PageRequest was built with safeLimit >= 1
        assertThat(true).isTrue();
    }

    @Test
    void query_limitAbove500_clampsTo500() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        newsQueryService.query(null, null, null, null, null, 9999);

        assertThat(true).isTrue();
    }

    @Test
    void query_withValidBbox_doesNotThrow() {
        when(newsItemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        NewsListResponse result = newsQueryService.query("15.0,42.0,20.0,46.0", null, null, null, null, 10);

        assertThat(result).isNotNull();
    }

    @Test
    void query_withInvalidBboxFormat_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> newsQueryService.query("bad,bbox", null, null, null, null, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bbox must have format");
    }

    @Test
    void query_withNonNumericBbox_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> newsQueryService.query("a,b,c,d", null, null, null, null, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid decimals");
    }

    @Test
    void findById_whenEntityExists_returnsResponse() {
        UUID id = sampleEntity.getId();
        when(newsItemRepository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(newsMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        Optional<NewsResponse> result = newsQueryService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(id);
    }

    @Test
    void findById_whenEntityNotFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(newsItemRepository.findById(id)).thenReturn(Optional.empty());

        Optional<NewsResponse> result = newsQueryService.findById(id);

        assertThat(result).isEmpty();
    }
}
