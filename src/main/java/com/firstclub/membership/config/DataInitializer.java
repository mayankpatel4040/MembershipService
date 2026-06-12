package com.firstclub.membership.config;

import com.firstclub.membership.entity.MembershipPlan;
import com.firstclub.membership.entity.MembershipTier;
import com.firstclub.membership.entity.TierBenefit;
import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.enums.BenefitType;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.DurationType;
import com.firstclub.membership.enums.Operators;
import com.firstclub.membership.enums.PlanStatus;
import com.firstclub.membership.repository.MembershipPlanRepository;
import com.firstclub.membership.repository.MembershipTierRepository;
import com.firstclub.membership.repository.TierBenefitRepository;
import com.firstclub.membership.repository.TierCriteriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class DataInitializer implements ApplicationRunner {

    private final MembershipTierRepository membershipTierRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final TierBenefitRepository tierBenefitRepository;
    private final TierCriteriaRepository tierCriteriaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTiers();
        seedPlans();
        Map<String, MembershipTier> tiers = membershipTierRepository.findAll().stream()
                .collect(Collectors.toMap(MembershipTier::getName, t -> t));
        seedBenefits(tiers);
        seedCriteria(tiers);
    }

    private void seedTiers() {
        if (membershipTierRepository.count() > 0) {
            log.info("MembershipTier already seeded, skipping");
            return;
        }
        membershipTierRepository.saveAll(List.of(
                tier("Silver", 1, "Entry tier — default on signup"),
                tier("Gold", 2, "Earned via spend or count"),
                tier("Platinum", 3, "Top tier — highest perks")
        ));
        log.info("Seeded tiers: Silver, Gold, Platinum");
    }

    private void seedPlans() {
        if (membershipPlanRepository.count() > 0) {
            log.info("MembershipPlan already seeded, skipping");
            return;
        }
        membershipPlanRepository.saveAll(List.of(
                plan("Monthly", DurationType.MONTHLY, 30, "199.00"),
                plan("Quarterly", DurationType.QUARTERLY, 90, "499.00"),
                plan("Yearly", DurationType.ANNUALLY, 365, "1499.00")
        ));
        log.info("Seeded plans: Monthly (199), Quarterly (499), Yearly (1499)");
    }

    private void seedBenefits(Map<String, MembershipTier> tiers) {
        if (tierBenefitRepository.count() > 0) {
            log.info("TierBenefit already seeded, skipping");
            return;
        }
        MembershipTier silver = tiers.get("Silver");
        MembershipTier gold = tiers.get("Gold");
        MembershipTier platinum = tiers.get("Platinum");

        tierBenefitRepository.saveAll(List.of(
                benefit(silver, BenefitType.FREE_DELIVERY, Map.of("min_order_value", 999)),
                benefit(silver, BenefitType.DISCOUNTS, Map.of("percent", 5, "categories", List.of("fashion"))),

                benefit(gold, BenefitType.FREE_DELIVERY, Map.of("min_order_value", 299)),
                benefit(gold, BenefitType.DISCOUNTS, Map.of("percent", 10, "categories", List.of("fashion", "home"))),
                benefit(gold, BenefitType.EARLY_ACCESS, Map.of("hours_before_sale", 24)),

                benefit(platinum, BenefitType.FREE_DELIVERY, Map.of("min_order_value", 0)),
                benefit(platinum, BenefitType.DISCOUNTS, Map.of("percent", 20, "categories", List.of("fashion", "home", "electronics", "grocery"))),
                benefit(platinum, BenefitType.EARLY_ACCESS, Map.of("hours_before_sale", 48))
        ));
        log.info("Seeded tier benefits");
    }

    private void seedCriteria(Map<String, MembershipTier> tiers) {
        if (tierCriteriaRepository.count() > 0) {
            log.info("TierCriteria already seeded, skipping");
            return;
        }
        MembershipTier gold = tiers.get("Gold");
        MembershipTier platinum = tiers.get("Platinum");

        tierCriteriaRepository.saveAll(List.of(
                criterion(gold, CriteriaType.ORDER_COUNT, 10, 30, Operators.GREATER_THAN_OR_EQUALS, Map.of(), 1),
                criterion(gold, CriteriaType.ORDER_VALUE, 10000.00, 30, Operators.GREATER_THAN_OR_EQUALS, Map.of(), 2),
                criterion(gold, CriteriaType.USER_COHORT, 0, 0, Operators.IN, Map.of("cohortType", "GOOD"), 3),

                criterion(platinum, CriteriaType.ORDER_COUNT, 30, 30, Operators.GREATER_THAN_OR_EQUALS, Map.of(), 10),
                criterion(platinum, CriteriaType.ORDER_VALUE, 50000.00, 30, Operators.GREATER_THAN_OR_EQUALS, Map.of(), 11)
        ));
        log.info("Seeded tier criteria");
    }

    private MembershipTier tier(String name, int level, String description) {
        MembershipTier t = new MembershipTier();
        t.setName(name);
        t.setLevel(level);
        t.setDescription(description);
        t.setActive(true);
        return t;
    }

    private MembershipPlan plan(String name, DurationType type, int days, String price) {
        MembershipPlan p = new MembershipPlan();
        p.setPlanName(name);
        p.setDurationType(type);
        p.setDurationInDays(days);
        p.setPrice(new BigDecimal(price));
        p.setStatus(PlanStatus.ACTIVE);
        return p;
    }

    private TierBenefit benefit(MembershipTier tier, BenefitType type, Map<String, Object> config) {
        TierBenefit b = new TierBenefit();
        b.setMembershipTier(tier);
        b.setBenefitType(type);
        b.setConfig(config);
        b.setActive(true);
        return b;
    }

    private TierCriteria criterion(MembershipTier tier, CriteriaType type, double threshold,
                                   int periodDays, Operators op, Map<String, Object> extra, int priority) {
        TierCriteria c = new TierCriteria();
        c.setMembershipTier(tier);
        c.setCriteriaType(type);
        c.setThresholdValue(threshold);
        c.setPeriodInDays(periodDays);
        c.setOperator(op);
        c.setAdditionalParameters(extra);
        c.setPriority(priority);
        return c;
    }
}
