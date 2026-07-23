package com.careerflow.notificationservice.notification.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(UUID eventId) {
        return processedEventRepository.existsById(eventId);
    }

    @Transactional
    public void markProcessed(UUID eventId, String eventType) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setEventType(eventType);
        processedEventRepository.save(processedEvent);
    }
}
