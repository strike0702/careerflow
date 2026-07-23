package com.careerflow.notificationservice.notification.messaging;

import com.careerflow.notificationservice.notification.handler.StubNotificationHandler;
import com.careerflow.notificationservice.notification.idempotency.IdempotencyService;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ApplicationEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventProcessor.class);

    private final IdempotencyService idempotencyService;
    private final StubNotificationHandler notificationHandler;
    private final Counter duplicateCounter;

    public ApplicationEventProcessor(
        IdempotencyService idempotencyService,
        StubNotificationHandler notificationHandler,
        MeterRegistry meterRegistry
    ) {
        this.idempotencyService = idempotencyService;
        this.notificationHandler = notificationHandler;
        this.duplicateCounter = meterRegistry.counter("careerflow.events.notification.duplicate");
    }

    @Transactional
    public void process(UUID eventId, String eventType, JsonNode envelope) {
        if (idempotencyService.alreadyProcessed(eventId)) {
            duplicateCounter.increment();
            log.info("Skipping duplicate event eventId={} eventType={}", eventId, eventType);
            return;
        }
        notificationHandler.handle(eventType, envelope);
        idempotencyService.markProcessed(eventId, eventType);
    }
}
