package com.firstclub.membership.config;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares every Kafka topic this service owns — both live topics and their
 * Dead Letter Topic counterparts. Spring's KafkaAdmin picks up NewTopic beans
 * on startup and creates any missing topics automatically.
 */
@Configuration
@Getter
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.order}")
    private String orderTopic;

    @Value("${app.kafka.topics.payment}")
    private String paymentTopic;

    @Value("${app.kafka.topics.order-dlt}")
    private String orderDltTopic;

    @Value("${app.kafka.topics.payment-dlt}")
    private String paymentDltTopic;

    // ── Live topics ──────────────────────────────────────────────────────────

    @Bean
    public NewTopic orderTopicDefinition() {
        return TopicBuilder.name(orderTopic).build();
    }

    @Bean
    public NewTopic paymentTopicDefinition() {
        return TopicBuilder.name(paymentTopic).build();
    }

    // ── Dead Letter Topics ───────────────────────────────────────────────────
    // Messages that exhaust all retries are published here so they can be
    // inspected and replayed without blocking the main partition.

    @Bean
    public NewTopic orderDltTopicDefinition() {
        return TopicBuilder.name(orderDltTopic).build();
    }

    @Bean
    public NewTopic paymentDltTopicDefinition() {
        return TopicBuilder.name(paymentDltTopic).build();
    }
}
