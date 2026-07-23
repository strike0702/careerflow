package com.careerflow.applicationservice.events.publisher;

import com.careerflow.applicationservice.AbstractIntegrationTest;
import com.careerflow.applicationservice.activity.repository.ActivityRepository;
import com.careerflow.applicationservice.application.dto.CreateApplicationRequest;
import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.application.repository.ApplicationRepository;
import com.careerflow.applicationservice.application.service.ApplicationService;
import com.careerflow.applicationservice.events.outbox.OutboxEventRepository;
import com.careerflow.applicationservice.events.outbox.OutboxEventStatus;
import com.careerflow.events.EventTopics;
import com.careerflow.applicationservice.shared.client.ResumeClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(
    partitions = 1,
    topics = {EventTopics.APPLICATION_EVENTS, EventTopics.APPLICATION_EVENTS_DLT}
)
@TestPropertySource(properties = {
    "careerflow.events.kafka.enabled=true",
    "careerflow.events.outbox.poll-interval-ms=3600000"
})
class OutboxPublishingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private ResumeClient resumeClient;

    @Autowired
    private OutboxPoller outboxPoller;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @BeforeEach
    void cleanUp() {
        activityRepository.deleteAll();
        outboxEventRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void pollAndPublish_sendsPendingOutboxEventToKafka() {
        applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Stripe",
                "Staff Engineer",
                "Remote",
                null,
                ApplicationSource.LINKEDIN,
                ApplicationStatus.APPLIED,
                null,
                null,
                null,
                null
            )
        );

        outboxPoller.pollAndPublish();

        assertThat(outboxEventRepository.findAll().getFirst().getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
            consumerProps,
            new StringDeserializer(),
            new StringDeserializer()
        ).createConsumer()) {
            consumer.subscribe(java.util.List.of(EventTopics.APPLICATION_EVENTS));
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                consumer,
                EventTopics.APPLICATION_EVENTS,
                Duration.ofSeconds(10)
            );
            assertThat(record.value()).contains("ApplicationCreated");
            assertThat(record.value()).contains("Stripe");
        }
    }
}
