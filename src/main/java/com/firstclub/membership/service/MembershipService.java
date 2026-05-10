package com.firstclub.membership.service;

import com.firstclub.membership.dto.MembershipPlanDetailResponse;
import com.firstclub.membership.dto.MembershipPlansResponse;
import com.firstclub.membership.dto.UserMembershipDetail;
import com.firstclub.membership.entity.*;
import com.firstclub.membership.enums.PlanStatus;
import com.firstclub.membership.enums.SubscriptionStatus;
import com.firstclub.membership.exception.*;
import com.firstclub.membership.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final UserRepository userRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public MembershipPlanDetailResponse getMembershipPlans(String phoneNumber) {
        User user = findUserOrThrow(phoneNumber);

        List<MembershipPlan> plans = membershipPlanRepository.findByStatus(PlanStatus.ACTIVE);
        if (plans.isEmpty()) {
            throw new PlanNotFoundException("No active plans available");
        }

        List<TierBenefit> benefits = tierBenefitRepository.findByMembershipTierId(user.getMembershipTier().getId());
        if (benefits.isEmpty()) {
            throw new MembershipException("No benefits configured for tier: " + user.getMembershipTier().getName(),
                    org.springframework.http.HttpStatus.NOT_FOUND);
        }

        List<MembershipPlansResponse> planResponses = plans.stream()
                .map(plan -> MembershipPlansResponse.builder()
                        .planName(plan.getPlanName())
                        .durationType(plan.getDurationType().getDisplayName())
                        .price(plan.getPrice())
                        .durationValue(plan.getDurationInDays())
                        .build())
                .toList();

        return MembershipPlanDetailResponse.builder()
                .membershipPlans(planResponses)
                .userTier(user.getMembershipTier().getName())
                .benefits(benefits.stream().map(b -> b.getBenefitType().getDisplayName()).toList())
                .build();
    }

    @Transactional
    public String subscribeToPlan(String phoneNumber, String planName) {
        User user = findUserOrThrow(phoneNumber);
        MembershipPlan plan = findActivePlanOrThrow(planName);

        UserMembership existing = userMembershipRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        if (existing != null) {
            throw new DuplicateSubscriptionException();
        }

        ZonedDateTime start = ZonedDateTime.now();
        userMembershipRepository.save(UserMembership.builder()
                .user(user)
                .membershipPlan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(start)
                .endDate(start.plusDays(plan.getDurationInDays()))
                .build());

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(null)
                .toMembershipPlan(plan)
                .build());

        return "Successfully subscribed to plan: " + planName;
    }

    @Transactional
    public String changePlan(String phoneNumber, String newPlanName) {
        User user = findUserOrThrow(phoneNumber);

        UserMembership existing = userMembershipRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        if (existing == null) {
            throw new NoActiveSubscriptionException();
        }

        if (existing.getMembershipPlan().getPlanName().equalsIgnoreCase(newPlanName)) {
            throw new MembershipException("User is already on plan: " + newPlanName,
                    org.springframework.http.HttpStatus.CONFLICT);
        }

        MembershipPlan newPlan = findActivePlanOrThrow(newPlanName);
        MembershipPlan previousPlan = existing.getMembershipPlan();

        existing.setStatus(SubscriptionStatus.CANCELLED);
        existing.setEndDate(ZonedDateTime.now());
        userMembershipRepository.save(existing);

        ZonedDateTime start = ZonedDateTime.now();
        userMembershipRepository.save(UserMembership.builder()
                .user(user)
                .membershipPlan(newPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(start)
                .endDate(start.plusDays(newPlan.getDurationInDays()))
                .build());

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(previousPlan)
                .toMembershipPlan(newPlan)
                .build());

        // Upgrade/downgrade is determined by price, not duration
        boolean isUpgrade = newPlan.getPrice().compareTo(previousPlan.getPrice()) > 0;
        return String.format("Successfully %s from %s to %s",
                isUpgrade ? "upgraded" : "downgraded",
                previousPlan.getPlanName(), newPlan.getPlanName());
    }

    @Transactional
    public String cancelSubscription(String phoneNumber) {
        User user = findUserOrThrow(phoneNumber);

        UserMembership existing = userMembershipRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        if (existing == null) {
            throw new NoActiveSubscriptionException();
        }

        existing.setStatus(SubscriptionStatus.CANCELLED);
        userMembershipRepository.save(existing);

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromMembershipPlan(existing.getMembershipPlan())
                .toMembershipPlan(null)
                .build());

        return "Successfully cancelled subscription for: " + phoneNumber;
    }

    @Transactional(readOnly = true)
    public UserMembershipDetail getUserMembershipDetails(String phoneNumber) {
        User user = findUserOrThrow(phoneNumber);

        UserMembership membership = userMembershipRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), SubscriptionStatus.ACTIVE);
        if (membership == null) {
            throw new NoActiveSubscriptionException();
        }

        return UserMembershipDetail.builder()
                .planName(membership.getMembershipPlan().getPlanName())
                .startDate(membership.getStartDate().toString())
                .endDate(membership.getEndDate().toString())
                .tier(Objects.nonNull(user.getMembershipTier()) ? user.getMembershipTier().getName() : null)
                .build();
    }

    private User findUserOrThrow(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UserNotFoundException(phoneNumber);
        }
        return user;
    }

    private MembershipPlan findActivePlanOrThrow(String planName) {
        MembershipPlan plan = membershipPlanRepository.findByPlanNameAndStatus(planName, PlanStatus.ACTIVE);
        if (plan == null) {
            throw new PlanNotFoundException(planName);
        }
        return plan;
    }
}
