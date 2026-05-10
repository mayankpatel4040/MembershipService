package com.firstclub.membership.service.RuleEvaluator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TierRuleEvaluatorFactory {

    private final List<TierRuleEvaluator> tierRuleEvaluators;

    public TierRuleEvaluator getTierRuleEvaluator(String tierType) {
        return tierRuleEvaluators.stream()
                .filter(evaluator -> evaluator.getTierType().name().equalsIgnoreCase(tierType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TierRuleEvaluator found for tier type: " + tierType));
    }
}
