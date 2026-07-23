package com.careerflow.applicationservice.events.publisher;

import com.careerflow.applicationservice.events.outbox.OutboxEvent;
import com.careerflow.applicationservice.events.outbox.OutboxEventRepository;
import com.careerflow.applicationservice.events.outbox.OutboxEventStatus;
import com.careerflow.events.CorrelationIdKafkaHelper;
import com.careerflow.events.EventObjectMappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private static final ObjectMapper OBJECT_MAPPER = EventObjectMappers.get();

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String applicationTopic;
    private final int batchSize;
    private final int maxAttempts;
    private final Counter publishedCounter;
    private final Counter failedCounter;

    public OutboxPoller(
        OutboxEventRepository outboxEventRepository,
        KafkaTemplate<String, String> kafkaTemplate,
        MeterRegistry meterRegistry,
        @Value("${careerflow.events.application-topic}") String applicationTopic,
        @Value("${careerflow.events.outbox.batch-size:50}") int batchSize,
        @Value("${careerflow.events.outbox.max-attempts:5}") int maxAttempts
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.applicationTopic = applicationTopic;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.publishedCounter = meterRegistry.counter("careerflow.events.outbox.published");
        this.failedCounter = meterRegistry.counter("careerflow.events.outbox.failed");
    }

    @Scheduled(fixedDelayString = "${careerflow.events.outbox.poll-interval-ms:5000}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingForUpdate(batchSize, maxAttempts);
        for (OutboxEvent outboxEvent : pendingEvents) {
            publishSingle(outboxEvent);
        }
    }

    private void publishSingle(OutboxEvent outboxEvent) {
        try {
            JsonNode envelope = OBJECT_MAPPER.readTree(outboxEvent.getPayloadJson());
            ProducerRecord<String, String> record = new ProducerRecord<>(
                applicationTopic,
                outboxEvent.getPartitionKey(),
                outboxEvent.getPayloadJson()
            );
            String requestId = envelope.path("metadata").path("requestId").asText(null);
            CorrelationIdKafkaHelper.applyToHeaders(requestId, record.headers());

            kafkaTemplate.send(record).get();
            outboxEvent.setStatus(OutboxEventStatus.PUBLISHED);
            outboxEvent.setPublishedAt(Instant.now());
            outboxEvent.setLastError(null);
            publishedCounter.increment();
            log.info(
                "Published outbox event eventId={} eventType={} aggregateId={}",
                outboxEvent.getEventId(),
                outboxEvent.getEventType(),
                outboxEvent.getAggregateId()
            );
        } catch (Exception ex) {
            outboxEvent.setAttempts(outboxEvent.getAttempts() + 1);
            outboxEvent.setLastError(ex.getMessage());
            if (outboxEvent.getAttempts() >= maxAttempts) {
                outboxEvent.setStatus(OutboxEventStatus.FAILED);
                failedCounter.increment();
                log.error(
                    "Outbox event permanently failed eventId={} eventType={} attempts={}",
                    outboxEvent.getEventId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getAttempts(),
                    ex
                );
            } else {
                log.warn(
                    "Outbox publish attempt failed eventId={} eventType={} attempts={}",
                    outboxEvent.getEventId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getAttempts(),
                    ex
                );
            }
        }
    }
}
