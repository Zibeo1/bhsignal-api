package com.geonews.integration.api;

import com.geonews.integration.api.dto.NewsListResponse;
import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.service.NewsQueryService;
import com.geonews.integration.service.NewsStreamService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1")
public class NewsController {

    private final NewsQueryService newsQueryService;
    private final NewsStreamService newsStreamService;

    public NewsController(NewsQueryService newsQueryService, NewsStreamService newsStreamService) {
        this.newsQueryService = newsQueryService;
        this.newsStreamService = newsStreamService;
    }

    @GetMapping("/news")
    public NewsListResponse listNews(
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return newsQueryService.query(bbox, from, to, category, location, limit);
    }

    @GetMapping("/news/{id}")
    public NewsResponse getNews(@PathVariable UUID id) {
        return newsQueryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "News item not found"));
    }

    @GetMapping(path = "/stream/news", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNews() {
        return newsStreamService.subscribe();
    }
}
