package com.firstclub.membership.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding for spring.kafka.consumer.* properties.
 * Adding a new consumer-level setting is just adding a record field + a YAML key —
 * no scattered @Value annotations needed.
 */
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public record KafkaConsumerProperties(
        String groupId,
        String autoOffsetReset,
        int concurrency
) {}
