package com.firstclub.membership.service.impl;

import com.firstclub.membership.entity.OrderAggregate;
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
import java.util.Optional;

@Component
@Log4j2
public class OrderValueEvaluator extends AbstractTierRuleEvaluator {

    private final OrderAggregateRepository orderAggregateRepository;

    public OrderValueEvaluator(TierCriteriaRepository tierCriteriaRepository,
                               OrderAggregateRepository orderAggregateRepository) {
        super(tierCriteriaRepository);
        this.orderAggregateRepository = orderAggregateRepository;
    }

    @Override
    public CriteriaType getTierType() {
        return CriteriaType.ORDER_VALUE;
    }

    @Override
    protected TierType doEvaluate(User user, List<TierCriteria> criteriaList) {
        String currentMonth = DateTimeUtil.getCurrentYearAndMonth();
        // Bug fix: was findByOrderMonthYear (no user filter) — now scoped to this user
        OrderAggregate aggregate = orderAggregateRepository
                .findByUserIdAndOrderMonthYear(user.getId(), currentMonth);
        if (aggregate == null) {
            log.info("No order aggregate for user {} in {}, defaulting to SILVER", user.getId(), currentMonth);
            return TierType.SILVER;
        }

        int bestPriority = -1;
        TierType assignedTier = TierType.SILVER;
        for (TierCriteria criteria : criteriaList) {
            if (aggregate.getTotalAmount().doubleValue() >= criteria.getThresholdValue()
                    && criteria.getPriority() > bestPriority) {
                bestPriority = criteria.getPriority();
                assignedTier = TierType.getByDisplayName(criteria.getMembershipTier().getName());
                log.info("User {} qualifies for tier {} via ORDER_VALUE (amount={}, threshold={})",
                        user.getId(), assignedTier, aggregate.getTotalAmount(), criteria.getThresholdValue());
            }
        }
        return assignedTier;
    }
}
