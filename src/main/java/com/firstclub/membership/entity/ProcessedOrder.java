package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

/**
 * Idempotent Consumer Pattern: tracks order IDs already processed by the Kafka consumer.
 * A unique constraint on order_id prevents double-counting from Kafka redeliveries.
 */
@Entity
@Data
@Table(name = "processed_orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_order_id", columnNames = "order_id"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private ZonedDateTime processedAt;
}
