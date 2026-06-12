package com.firstclub.membership.service.impl;

import com.firstclub.membership.entity.TierCriteria;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.TierType;
import com.firstclub.membership.repository.OrderAggregateRepository;
import com.firstclub.membership.repository.TierCriteriaRepository;
import com.firstclub.membership.service.RuleEvaluator.AbstractTierRuleEvaluator;
import com.firstclub.membership.utils.DateTimeUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class OrderCountEvaluator extends AbstractTierRuleEvaluator {

    private final OrderAggregateRepository orderAggregateRepository;

    public OrderCountEvaluator(TierCriteriaRepository tierCriteriaRepository,
                               OrderAggregateRepository orderAggregateRepository) {
        super(tierCriteriaRepository);
        this.orderAggregateRepository = orderAggregateRepository;
    }

    @Override
    public CriteriaType getTierType() {
        return CriteriaType.ORDER_COUNT;
    }

    @Override
    protected TierType doEvaluate(User user, List<TierCriteria> criteriaList) {
        String currentMonth = DateTimeUtil.getCurrentYearAndMonth();
        int orderCount = orderAggregateRepository.sumOrderCountByUserAndMonth(user, currentMonth);
        if (orderCount == 0) {
            log.info("No orders for user {} in {}, defaulting to SILVER", user.getId(), currentMonth);
            return TierType.SILVER;
        }

        int bestPriority = -1;
        TierType assignedTier = TierType.SILVER;
        for (TierCriteria criteria : criteriaList) {
            if (orderCount >= criteria.getThresholdValue() && criteria.getPriority() > bestPriority) {
                bestPriority = criteria.getPriority();
                assignedTier = TierType.getByDisplayName(criteria.getMembershipTier().getName());
                log.info("User {} qualifies for tier {} via ORDER_COUNT (count={}, threshold={})",
                        user.getId(), assignedTier, orderCount, criteria.getThresholdValue());
            }
        }
        return assignedTier;
    }
}
