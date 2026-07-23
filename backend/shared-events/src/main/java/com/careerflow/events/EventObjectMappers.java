package com.careerflow.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Dedicated Jackson mapper for Kafka/outbox payloads. Not a Spring bean so it
 * cannot replace Boot's HTTP {@code ObjectMapper} (which serializes dates as ISO-8601).
 */
public final class EventObjectMappers {

    private static final ObjectMapper INSTANCE = build();

    private EventObjectMappers() {
    }

    public static ObjectMapper get() {
        return INSTANCE;
    }

    private static ObjectMapper build() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
