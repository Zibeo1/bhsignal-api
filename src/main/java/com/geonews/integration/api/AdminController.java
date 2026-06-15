package com.geonews.integration.api;

import com.geonews.integration.config.AppProperties;
import com.geonews.integration.repository.NewsItemRepository;
import com.geonews.integration.repository.ProcessedEventRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Server-to-server maintenance endpoints, guarded by the shared webhook secret.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final NewsItemRepository newsItemRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final AppProperties appProperties;

    public AdminController(
            NewsItemRepository newsItemRepository,
            ProcessedEventRepository processedEventRepository,
            AppProperties appProperties
    ) {
        this.newsItemRepository = newsItemRepository;
        this.processedEventRepository = processedEventRepository;
        this.appProperties = appProperties;
    }

    @DeleteMapping("/news")
    @Transactional
    public Map<String, Object> purgeNews(
            @RequestHeader(name = "X-Admin-Token", required = false) String adminToken
    ) {
        if (adminToken == null || !adminToken.equals(appProperties.getWebhook().getSecret())) {
            throw new ResponseStatusException(FORBIDDEN, "Invalid admin token");
        }

        long deleted = newsItemRepository.count();
        newsItemRepository.deleteAllInBatch();
        processedEventRepository.deleteAllInBatch();
        return Map.of("deleted", deleted);
    }
}
