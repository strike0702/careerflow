package com.careerflow.notificationservice.notification.messaging;

import com.careerflow.events.CorrelationIdKafkaHelper;
import com.careerflow.events.EventObjectMappers;
import com.careerflow.events.EventTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "careerflow.events.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventListener.class);

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
        EventTypes.APPLICATION_CREATED,
        EventTypes.APPLICATION_STATUS_CHANGED,
        EventTypes.OFFER_ADDED,
        EventTypes.OFFER_UPDATED
    );

    private static final ObjectMapper OBJECT_MAPPER = EventObjectMappers.get();

    private final ApplicationEventProcessor eventProcessor;
    private final Counter consumedCounter;

    public ApplicationEventListener(
        ApplicationEventProcessor eventProcessor,
        MeterRegistry meterRegistry
    ) {
        this.eventProcessor = eventProcessor;
        this.consumedCounter = meterRegistry.counter("careerflow.events.notification.consumed");
    }

    @KafkaListener(
        topics = "${careerflow.events.application-topic}",
        groupId = "notification-service"
    )
    public void onApplicationEvent(ConsumerRecord<String, String> record) {
        String requestId = CorrelationIdKafkaHelper.extractFromHeaders(record.headers());
        CorrelationIdKafkaHelper.bindMdc(requestId);
        try {
            JsonNode envelope = OBJECT_MAPPER.readTree(record.value());
            String eventType = envelope.path("eventType").asText();
            UUID eventId = UUID.fromString(envelope.path("eventId").asText());

            if (!SUPPORTED_EVENT_TYPES.contains(eventType)) {
                log.warn("Ignoring unsupported eventType={} eventId={}", eventType, eventId);
                return;
            }

            eventProcessor.process(eventId, eventType, envelope);
            consumedCounter.increment();
        } catch (Exception ex) {
            log.error("Failed to process application event partition={} offset={}", record.partition(), record.offset(), ex);
            throw new IllegalStateException("Failed to process application event", ex);
        } finally {
            CorrelationIdKafkaHelper.clearMdc();
        }
    }
}
