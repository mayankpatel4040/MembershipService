package com.firstclub.membership.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Central Kafka consumer configuration. Implements five architectural concerns:
 *
 *  1. @ConfigurationProperties  — type-safe property binding via KafkaConsumerProperties
 *  2. Dead Letter Topic + Retry  — failed messages are retried then parked in <topic>.DLT
 *  3. Manual Acknowledgement    — offsets committed only after business logic succeeds
 *  4. Per-topic factories        — orderContainerFactory (concurrent) vs paymentContainerFactory (serial)
 *  5. Micrometer observability  — consumer lag + processing latency metrics auto-exported
 */
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class KafkaConsumerConfig {

    private final String bootstrapServers;
    private final KafkaConsumerProperties consumerProps;
    private final int retryMaxAttempts;
    private final long retryIntervalMs;

    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            KafkaConsumerProperties consumerProps,
            @Value("${app.kafka.retry.max-attempts}") int retryMaxAttempts,
            @Value("${app.kafka.retry.interval-ms}") long retryIntervalMs) {
        this.bootstrapServers = bootstrapServers;
        this.consumerProps = consumerProps;
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryIntervalMs = retryIntervalMs;
    }

    // ── ConsumerFactory ──────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(buildConsumerProperties());
    }

    // ── Error Handler (Dead Letter Topic + Retry) ────────────────────────────

    /**
     * On each failure Spring retries up to retryMaxAttempts times with a fixed
     * interval, then hands the record to the DeadLetterPublishingRecoverer which
     * writes it to <original-topic>.DLT so processing can continue unblocked.
     */
    @Bean
    public DefaultErrorHandler deadLetterErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                // Route to partition 0 of the DLT — keeps DLT ordering simple
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", 0));

        // FixedBackOff(intervalMs, maxAttempts): retry N times, then invoke recoverer
        var backOff = new FixedBackOff(retryIntervalMs, retryMaxAttempts);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    // ── Per-Topic Container Factories ────────────────────────────────────────

    /**
     * Order events: higher concurrency is safe because orders are idempotent
     * (ProcessedOrder table guards against duplicates).
     */
    @Bean("orderContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> orderContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler) {
        return buildContainerFactory(consumerFactory, errorHandler, consumerProps.concurrency());
    }

    /**
     * Payment events: single thread enforces strict message ordering.
     * Concurrency > 1 would allow out-of-order processing within the same partition.
     */
    @Bean("paymentContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> paymentContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler) {
        return buildContainerFactory(consumerFactory, errorHandler, 1);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Shared factory builder. Every factory gets:
     * - MANUAL_IMMEDIATE ack mode (offsets only committed on explicit ack.acknowledge())
     * - the shared DLT error handler
     * - Micrometer metrics (kafka.consumer.* gauges + timers)
     */
    private ConcurrentKafkaListenerContainerFactory<String, String> buildContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler,
            int concurrency) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setMicrometerEnabled(true);
        return factory;
    }

    /**
     * Consumer client properties. enable.auto.commit=false pairs with MANUAL_IMMEDIATE
     * to ensure the broker offset only advances after the listener calls ack.acknowledge().
     */
    private Map<String, Object> buildConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG,                 consumerProps.groupId(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,        consumerProps.autoOffsetReset(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,       false
        );
    }
}
