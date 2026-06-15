package com.geonews.integration.service;

import com.geonews.integration.api.dto.NewsListResponse;
import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.config.AppProperties;
import com.geonews.integration.domain.NewsItemEntity;
import com.geonews.integration.repository.NewsItemRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsQueryService {

    // Approximate geographic bounding box of Bosnia and Herzegovina.
    private static final double BIH_MIN_LON = 15.70;
    private static final double BIH_MIN_LAT = 42.55;
    private static final double BIH_MAX_LON = 19.65;
    private static final double BIH_MAX_LAT = 45.30;

    private final NewsItemRepository newsItemRepository;
    private final NewsMapper newsMapper;
    private final AppProperties appProperties;

    public NewsQueryService(
            NewsItemRepository newsItemRepository,
            NewsMapper newsMapper,
            AppProperties appProperties
    ) {
        this.newsItemRepository = newsItemRepository;
        this.newsMapper = newsMapper;
        this.appProperties = appProperties;
    }

    public NewsListResponse query(
            String bbox,
            Instant from,
            Instant to,
            String category,
            String location,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 500));

        Specification<NewsItemEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), to));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("locationName")), "%" + location.toLowerCase() + "%"));
            }

            if (bbox != null && !bbox.isBlank()) {
                double[] parsedBbox = parseBboxOrThrow(bbox);
                double minLon = parsedBbox[0];
                double minLat = parsedBbox[1];
                double maxLon = parsedBbox[2];
                double maxLat = parsedBbox[3];

                predicates.add(cb.isNotNull(root.get("latitude")));
                predicates.add(cb.isNotNull(root.get("longitude")));
                predicates.add(cb.between(root.get("latitude"), minLat, maxLat));
                predicates.add(cb.between(root.get("longitude"), minLon, maxLon));
            }

            if (appProperties.getNews().isBosniaOnly()) {
                predicates.add(cb.isNotNull(root.get("latitude")));
                predicates.add(cb.isNotNull(root.get("longitude")));
                predicates.add(cb.between(root.get("latitude"), BIH_MIN_LAT, BIH_MAX_LAT));
                predicates.add(cb.between(root.get("longitude"), BIH_MIN_LON, BIH_MAX_LON));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        List<NewsResponse> responses = newsItemRepository.findAll(
                        spec,
                        PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "publishedAt"))
                )
                .stream()
                .map(newsMapper::toResponse)
                .toList();

        String nextSince = responses.isEmpty()
                ? null
                : DateTimeFormatter.ISO_INSTANT.format(responses.get(responses.size() - 1).publishedAt());

        return new NewsListResponse(responses, nextSince);
    }

    public Optional<NewsResponse> findById(UUID id) {
        return newsItemRepository.findById(id).map(newsMapper::toResponse);
    }

    private double[] parseBboxOrThrow(String bbox) {
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("bbox must have format minLon,minLat,maxLon,maxLat");
        }

        try {
            double minLon = Double.parseDouble(parts[0]);
            double minLat = Double.parseDouble(parts[1]);
            double maxLon = Double.parseDouble(parts[2]);
            double maxLat = Double.parseDouble(parts[3]);
            return new double[]{minLon, minLat, maxLon, maxLat};
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("bbox values must be valid decimals");
        }
    }
}
