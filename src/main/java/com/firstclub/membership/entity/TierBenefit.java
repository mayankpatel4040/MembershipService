package com.firstclub.membership.entity;

import com.firstclub.membership.enums.BenefitType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Map;

@Data
@Entity
@Table(name = "tier_benefits")
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tier_benefit_membership_tier"))
    private MembershipTier membershipTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false)
    private BenefitType benefitType;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "JSONB", nullable = false)
    private Map<String, Object> config;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private java.time.ZonedDateTime updatedAt;
}
