package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(
        name = "order_aggregate",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_aggregate_user_month",
                columnNames = {"user_id", "order_month_year"}
        )
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAggregate {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_aggregate_user"))
    private User user;

    @Column(name = "order_month_year", nullable = false)
    private String orderMonthYear; // Format: YYYY-MM

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "order_count", nullable = false)
    private int orderCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
