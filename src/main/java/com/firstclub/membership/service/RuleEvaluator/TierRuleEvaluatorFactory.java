package com.firstclub.membership.service.RuleEvaluator;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TierRuleEvaluatorFactory {

    private final Map<String, TierRuleEvaluator> evaluatorMap;

    public TierRuleEvaluatorFactory(List<TierRuleEvaluator> evaluators) {
        this.evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(e -> e.getTierType().name(), e -> e));
    }

    public TierRuleEvaluator getTierRuleEvaluator(String tierType) {
        TierRuleEvaluator evaluator = evaluatorMap.get(tierType.toUpperCase());
        if (evaluator == null) {
            throw new IllegalArgumentException("No TierRuleEvaluator found for tier type: " + tierType);
        }
        return evaluator;
    }
}
