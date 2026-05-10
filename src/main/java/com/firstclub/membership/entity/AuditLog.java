package com.firstclub.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Data
@Table(name = "audit_log")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_plan", foreignKey = @ForeignKey(name = "fk_audit_log_from_plan"))
    private MembershipPlan fromMembershipPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_plan", foreignKey = @ForeignKey(name = "fk_audit_log_to_plan"))
    private MembershipPlan toMembershipPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_tier", foreignKey = @ForeignKey(name = "fk_audit_log_from_tier"))
    private MembershipTier fromTier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_tier", foreignKey = @ForeignKey(name = "fk_audit_log_to_tier"))
    private MembershipTier toTier;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "JSONB")
    private Map<String, Object> config;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
