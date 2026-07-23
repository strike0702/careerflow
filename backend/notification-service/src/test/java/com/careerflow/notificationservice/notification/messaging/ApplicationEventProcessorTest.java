package com.careerflow.notificationservice.notification.messaging;

import com.careerflow.events.EventTypes;
import com.careerflow.notificationservice.AbstractIntegrationTest;
import com.careerflow.notificationservice.notification.idempotency.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationEventProcessorTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventProcessor eventProcessor;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        processedEventRepository.deleteAll();
    }

    @Test
    void process_marksEventProcessedAndSkipsDuplicates() throws Exception {
        UUID eventId = UUID.randomUUID();
        String envelopeJson = """
            {
              "specVersion": "1.0",
              "eventId": "%s",
              "eventType": "ApplicationCreated",
              "eventVersion": 1,
              "occurredAt": "2026-07-17T08:52:00.123Z",
              "producer": "application-service",
              "aggregateType": "Application",
              "aggregateId": "550e8400-e29b-41d4-a716-446655440001",
              "metadata": {
                "requestId": "req-123",
                "userId": "user-a"
              },
              "payload": {
                "applicationId": "550e8400-e29b-41d4-a716-446655440001",
                "companyName": "Stripe",
                "jobTitle": "Engineer",
                "status": "APPLIED",
                "location": "Remote",
                "source": "LINKEDIN"
              }
            }
            """.formatted(eventId);

        var envelope = objectMapper.readTree(envelopeJson);
        eventProcessor.process(eventId, EventTypes.APPLICATION_CREATED, envelope);
        eventProcessor.process(eventId, EventTypes.APPLICATION_CREATED, envelope);

        assertThat(processedEventRepository.findAll()).hasSize(1);
    }
}
