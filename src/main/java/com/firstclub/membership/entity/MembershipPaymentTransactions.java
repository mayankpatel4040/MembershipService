package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "membership_payment_transactions")
public class MembershipPaymentTransactions {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_membership_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_membership"))
    private UserMembership userMembership;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "external_payment_id", nullable = false)
    private String externalPaymentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
