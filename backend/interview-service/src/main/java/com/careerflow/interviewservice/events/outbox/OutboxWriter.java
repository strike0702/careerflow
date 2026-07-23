package com.careerflow.interviewservice.events.outbox;

import com.careerflow.common.observability.CorrelationIdConstants;
import com.careerflow.events.DomainEventEnvelope;
import com.careerflow.events.EventObjectMappers;
import com.careerflow.interviewservice.events.mapping.InterviewEventMapper;
import com.careerflow.interviewservice.interview.model.Interview;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxWriter {

    private static final ObjectMapper OBJECT_MAPPER = EventObjectMappers.get();

    private final OutboxEventRepository outboxEventRepository;
    private final InterviewEventMapper eventMapper;

    public OutboxWriter(
        OutboxEventRepository outboxEventRepository,
        InterviewEventMapper eventMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventMapper = eventMapper;
    }

    public void writeInterviewScheduled(Interview interview, String userId) {
        write(eventMapper.interviewScheduled(interview, userId, currentRequestId()));
    }

    public void writeInterviewCompleted(Interview interview, String userId) {
        write(eventMapper.interviewCompleted(interview, userId, currentRequestId()));
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
