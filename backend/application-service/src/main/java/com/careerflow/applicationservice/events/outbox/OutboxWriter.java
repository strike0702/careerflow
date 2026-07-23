package com.careerflow.applicationservice.events.outbox;

import com.careerflow.applicationservice.events.mapping.ApplicationEventMapper;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.common.observability.CorrelationIdConstants;
import com.careerflow.events.DomainEventEnvelope;
import com.careerflow.events.EventObjectMappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxWriter {

    private static final ObjectMapper OBJECT_MAPPER = EventObjectMappers.get();

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventMapper eventMapper;

    public OutboxWriter(
        OutboxEventRepository outboxEventRepository,
        ApplicationEventMapper eventMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventMapper = eventMapper;
    }

    public void writeApplicationCreated(Application application, String userId) {
        write(eventMapper.applicationCreated(application, userId, currentRequestId()));
    }

    public void writeApplicationStatusChanged(
        Application application,
        ApplicationStatus previousStatus,
        String userId
    ) {
        write(eventMapper.applicationStatusChanged(application, previousStatus, userId, currentRequestId()));
    }

    public void writeOfferAdded(Application application, Offer offer, String userId) {
        write(eventMapper.offerAdded(application, offer, userId, currentRequestId()));
    }

    public void writeOfferUpdated(Application application, Offer offer, String userId) {
        write(eventMapper.offerUpdated(application, offer, userId, currentRequestId()));
    }

    private void write(DomainEventEnvelope<?> envelope) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(envelope.eventId());
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setAggregateId(envelope.aggregateId());
        outboxEvent.setPartitionKey(envelope.aggregateId().toString());
        outboxEvent.setPayloadJson(serialize(envelope));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(DomainEventEnvelope<?> envelope) {
        try {
            return OBJECT_MAPPER.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event " + envelope.eventId(), ex);
        }
    }

    private String currentRequestId() {
        String requestId = MDC.get(CorrelationIdConstants.MDC_KEY);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
