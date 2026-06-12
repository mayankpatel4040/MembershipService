package com.firstclub.membership.service;

import com.firstclub.membership.entity.AuditLog;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.TierType;
import com.firstclub.membership.repository.AuditLogRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.UserRepository;
import com.firstclub.membership.service.RuleEvaluator.TierRuleEvaluator;
import com.firstclub.membership.service.RuleEvaluator.TierRuleEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Log4j2
public class TierRuleEvaluatorService {

    private final TierRuleEvaluatorFactory tierRuleEvaluatorFactory;
    private final UserRepository userRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void evaluateRules(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            log.error("User not found with phone number: {}", phoneNumber);
            return;
        }

        MembershipTier currentTier = user.getMembershipTier();
        if (currentTier != null && TierType.PLATINUM.getDisplayName().equalsIgnoreCase(currentTier.getName())) {
            log.info("User {} already Platinum, skipping evaluation", user.getId());
            return;
        }

        TierType assignedTier = TierType.SILVER;
        for (CriteriaType criteriaType : CriteriaType.values()) {
            try {
                TierRuleEvaluator tierRuleEvaluator =
                        tierRuleEvaluatorFactory.getTierRuleEvaluator(criteriaType.name());
                TierType candidate = tierRuleEvaluator.evaluateRule(user);
                log.info("Evaluator [{}] -> {} for user {}", criteriaType, candidate, user.getId());
                if (candidate != null && candidate.getRank() > assignedTier.getRank()) {
                    assignedTier = candidate;
                }
            } catch (IllegalArgumentException e) {
                log.error("Failed to retrieve TierRuleEvaluator for criteria type: {}. Error: {}",
                        criteriaType, e.getMessage());
            }
        }

        if (currentTier != null && currentTier.getName().equalsIgnoreCase(assignedTier.getDisplayName())) {
            log.info("User {} already on tier {}, skipping reassignment", user.getId(), assignedTier);
            return;
        }

        MembershipTier membershipTier = membershipTierRepository.findByName(assignedTier.getDisplayName());
        user.setMembershipTier(membershipTier);
        log.info("Assigned tier for user {}: {} (was {})",
                user.getId(), assignedTier,
                currentTier != null ? currentTier.getName() : "none");

        auditLogRepository.save(AuditLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .fromTier(currentTier)
                .toTier(membershipTier)
                .build());
    }
}
