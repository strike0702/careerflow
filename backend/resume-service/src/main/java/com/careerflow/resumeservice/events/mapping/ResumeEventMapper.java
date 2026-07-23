package com.careerflow.resumeservice.events.mapping;

import com.careerflow.events.DomainEventEnvelope;
import com.careerflow.events.EventMetadata;
import com.careerflow.events.EventTypes;
import com.careerflow.events.payload.ResumeDeletedPayload;
import com.careerflow.events.payload.ResumeUploadedPayload;
import com.careerflow.resumeservice.resume.model.Resume;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ResumeEventMapper {

    public DomainEventEnvelope<ResumeUploadedPayload> resumeUploaded(
        Resume resume,
        String userId,
        String requestId
    ) {
        ResumeUploadedPayload payload = new ResumeUploadedPayload(
            resume.getId(),
            resume.getLabel(),
            resume.getFileName(),
            resume.getContentType(),
            resume.getFileSizeBytes(),
            resume.getStorageUrl(),
            resume.getVersionNo()
        );
        return envelope(EventTypes.RESUME_UPLOADED, resume.getId(), userId, requestId, payload);
    }

    public DomainEventEnvelope<ResumeDeletedPayload> resumeDeleted(
        UUID resumeId,
        String userId,
        String requestId
    ) {
        return envelope(EventTypes.RESUME_DELETED, resumeId, userId, requestId, new ResumeDeletedPayload(resumeId));
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
            DomainEventEnvelope.PRODUCER_RESUME_SERVICE,
            DomainEventEnvelope.AGGREGATE_RESUME,
            aggregateId,
            new EventMetadata(requestId, userId),
            payload
        );
    }
}
