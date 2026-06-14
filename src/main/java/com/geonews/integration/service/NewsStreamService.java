package com.geonews.integration.service;

import com.geonews.integration.api.dto.NewsResponse;
import com.geonews.integration.api.dto.NewsStreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NewsStreamService {

    private static final Logger log = LoggerFactory.getLogger(NewsStreamService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((error) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("stream.ready")
                    .data(new NewsStreamEvent("stream.ready", Instant.now(), null))
            );
        } catch (IOException ex) {
            emitter.complete();
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(String eventType, NewsResponse newsItem) {
        NewsStreamEvent payload = new NewsStreamEvent(eventType, Instant.now(), newsItem);

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(payload));
            } catch (IOException ex) {
                log.debug("SSE emitter dropped: {}", ex.getMessage());
                emitter.complete();
                emitters.remove(emitter);
            }
        });
    }
}
