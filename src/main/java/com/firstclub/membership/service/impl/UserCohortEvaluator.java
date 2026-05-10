package com.firstclub.membership.service.impl;

import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CohortType;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.TierType;
import com.firstclub.membership.repository.TierCriteriaRepository;
import com.firstclub.membership.service.RuleEvaluator.AbstractTierRuleEvaluator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class UserCohortEvaluator extends AbstractTierRuleEvaluator {

    public UserCohortEvaluator(TierCriteriaRepository tierCriteriaRepository) {
        super(tierCriteriaRepository);
    }

    @Override
    public CriteriaType getTierType() {
        return CriteriaType.USER_COHORT;
    }

    @Override
    protected TierType doEvaluate(User user, List<TierCriteria> criteriaList) {
        for (TierCriteria criteria : criteriaList) {
            Map<String, Object> params = criteria.getAdditionalParameters();
            if (params == null || !params.containsKey("cohortType")) {
                log.warn("USER_COHORT criteria id={} missing 'cohortType' parameter, skipping", criteria.getId());
                continue;
            }
            try {
                CohortType cohortType = CohortType.valueOf(String.valueOf(params.get("cohortType")).toUpperCase());
                if (user.getCohortType() == cohortType) {
                    TierType tier = TierType.getByDisplayName(criteria.getMembershipTier().getName());
                    log.info("User {} cohort {} matched criteria id={}, assigning tier {}",
                            user.getId(), cohortType, criteria.getId(), tier);
                    return tier;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid cohortType value '{}' in criteria id={}, skipping",
                        params.get("cohortType"), criteria.getId());
            }
        }
        return TierType.SILVER;
    }
}
