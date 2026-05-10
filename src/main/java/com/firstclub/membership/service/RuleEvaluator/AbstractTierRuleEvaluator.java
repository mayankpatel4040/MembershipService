package com.firstclub.membership.service.RuleEvaluator;

import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.TierType;
import com.firstclub.membership.repository.TierCriteriaRepository;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Template Method Pattern: defines the skeleton of the tier-evaluation algorithm.
 * Subclasses implement doEvaluate() with their specific criteria logic;
 * this class owns the fetch-and-guard steps that are identical across all evaluators.
 */
@Log4j2
public abstract class AbstractTierRuleEvaluator implements TierRuleEvaluator {

    protected final TierCriteriaRepository tierCriteriaRepository;

    protected AbstractTierRuleEvaluator(TierCriteriaRepository tierCriteriaRepository) {
        this.tierCriteriaRepository = tierCriteriaRepository;
    }

    @Override
    public final TierType evaluateRule(User user) {
        List<TierCriteria> criteriaList = tierCriteriaRepository.findByCriteriaType(getTierType());
        if (criteriaList.isEmpty()) {
            log.info("No criteria configured for {}, defaulting to SILVER", getTierType());
            return TierType.SILVER;
        }
        return doEvaluate(user, criteriaList);
    }

    protected abstract TierType doEvaluate(User user, List<TierCriteria> criteriaList);
}
