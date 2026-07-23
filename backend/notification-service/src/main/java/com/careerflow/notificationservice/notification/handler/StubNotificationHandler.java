package com.careerflow.notificationservice.notification.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(StubNotificationHandler.class);

    public void handle(String eventType, JsonNode envelope) {
        JsonNode metadata = envelope.path("metadata");
        JsonNode payload = envelope.path("payload");
        String eventId = envelope.path("eventId").asText();
        String userId = metadata.path("userId").asText(null);
        String requestId = metadata.path("requestId").asText(null);
        String applicationId = payload.path("applicationId").asText(null);

        log.info(
            "Stub notification eventType={} eventId={} userId={} applicationId={} requestId={} action=would_notify",
            eventType,
            eventId,
            userId,
            applicationId,
            requestId
        );
    }
}
