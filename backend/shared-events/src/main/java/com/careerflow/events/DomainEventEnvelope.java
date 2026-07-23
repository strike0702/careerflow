package com.careerflow.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainEventEnvelope<T>(
    String specVersion,
    UUID eventId,
    String eventType,
    int eventVersion,
    Instant occurredAt,
    String producer,
    String aggregateType,
    UUID aggregateId,
    EventMetadata metadata,
    T payload
) {

    public static final String SPEC_VERSION = "1.0";
    public static final String PRODUCER_APPLICATION_SERVICE = "application-service";
    public static final String PRODUCER_RESUME_SERVICE = "resume-service";
    public static final String PRODUCER_INTERVIEW_SERVICE = "interview-service";
    public static final String AGGREGATE_APPLICATION = "Application";
    public static final String AGGREGATE_RESUME = "Resume";
    public static final String AGGREGATE_INTERVIEW = "Interview";
}
