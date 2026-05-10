package com.firstclub.membership.entity;

import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.Operators;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Data
@Table(name = "tier_criteria")
public class TierCriteria {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tier_criteria_membership_tier"))
    private MembershipTier membershipTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false)
    private CriteriaType criteriaType;

    @Column(name = "threshold_value")
    private double thresholdValue;

    @Column(name = "period_in_days")
    private int periodInDays;   // added to specify the time frame for criteria evaluation

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private Operators operator;  // added to specify how the threshold value should be evaluated (e.g., GREATER_THAN, LESS_THAN, EQUAL_TO)

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "additional_parameters", columnDefinition = "JSONB", nullable = false)
    private Map<String, Object> additionalParameters;

    @Column(name = "priority", nullable = false)
    private int priority;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
