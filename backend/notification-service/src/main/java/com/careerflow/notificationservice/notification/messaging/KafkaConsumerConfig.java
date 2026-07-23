package com.careerflow.notificationservice.notification.messaging;

import com.careerflow.events.EventTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "careerflow.events.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    @Bean
    public NewTopic applicationEventsTopic(
        @Value("${careerflow.events.application-topic}") String applicationTopic
    ) {
        return new NewTopic(applicationTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic applicationEventsDltTopic() {
        return new NewTopic(EventTopics.APPLICATION_EVENTS_DLT, 1, (short) 1);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
        KafkaTemplate<String, String> kafkaTemplate,
        @Value("${careerflow.events.consumer.retry.max-attempts:3}") long maxAttempts,
        @Value("${careerflow.events.consumer.retry.backoff-ms:1000}") long backoffMs
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(EventTopics.APPLICATION_EVENTS_DLT, record.partition())
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(backoffMs, maxAttempts - 1));
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
            org.slf4j.LoggerFactory.getLogger(KafkaConsumerConfig.class).warn(
                "Retrying Kafka message topic={} partition={} offset={} attempt={}",
                record.topic(),
                record.partition(),
                record.offset(),
                deliveryAttempt,
                ex
            )
        );
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory,
        DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
