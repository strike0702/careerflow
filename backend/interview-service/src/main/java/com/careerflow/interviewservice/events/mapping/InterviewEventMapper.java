package com.careerflow.interviewservice.events.mapping;

import com.careerflow.events.DomainEventEnvelope;
import com.careerflow.events.EventMetadata;
import com.careerflow.events.EventTypes;
import com.careerflow.events.payload.InterviewCompletedPayload;
import com.careerflow.events.payload.InterviewScheduledPayload;
import com.careerflow.interviewservice.interview.model.Interview;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class InterviewEventMapper {

    public DomainEventEnvelope<InterviewScheduledPayload> interviewScheduled(
        Interview interview,
        String userId,
        String requestId
    ) {
        InterviewScheduledPayload payload = new InterviewScheduledPayload(
            interview.getId(),
            interview.getApplicationId(),
            interview.getRoundType().name(),
            interview.getScheduledAt().toString(),
            interview.getMode().name()
        );
        return envelope(EventTypes.INTERVIEW_SCHEDULED, interview.getId(), userId, requestId, payload);
    }

    public DomainEventEnvelope<InterviewCompletedPayload> interviewCompleted(
        Interview interview,
        String userId,
        String requestId
    ) {
        InterviewCompletedPayload payload = new InterviewCompletedPayload(
            interview.getId(),
            interview.getApplicationId(),
            interview.getOutcome().name(),
            Instant.now().toString()
        );
        return envelope(EventTypes.INTERVIEW_COMPLETED, interview.getId(), userId, requestId, payload);
    }

    private <T> DomainEventEnvelope<T> envelope(
        String eventType,
        UUID aggregateId,
        String userId,
        String requestId,
        T payload
    ) {
        return new DomainEventEnvelope<>(
            DomainEventEnvelope.SPEC_VERSION,
            UUID.randomUUID(),
            eventType,
            1,
            Instant.now(),
            DomainEventEnvelope.PRODUCER_INTERVIEW_SERVICE,
            DomainEventEnvelope.AGGREGATE_INTERVIEW,
            aggregateId,
            new EventMetadata(requestId, userId),
            payload
        );
    }
}
