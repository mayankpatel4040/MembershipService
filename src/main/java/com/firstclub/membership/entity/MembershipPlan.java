package com.firstclub.membership.entity;

import com.firstclub.membership.enums.DurationType;
import com.firstclub.membership.enums.PlanStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private DurationType durationType;

    @Column(name = "duration_in_days", nullable = false)
    private int durationInDays;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Convert(converter = PlanStatus.PersistenceConverter.class)
    @Column(name = "status", nullable = false)
    private PlanStatus status;
}
