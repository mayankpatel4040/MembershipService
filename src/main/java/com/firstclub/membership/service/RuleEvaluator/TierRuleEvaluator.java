package com.firstclub.membership.service.RuleEvaluator;

import com.firstclub.membership.entity.User;
import com.firstclub.membership.enums.CriteriaType;
import com.firstclub.membership.enums.TierType;

public interface TierRuleEvaluator {

    CriteriaType getTierType();

    TierType evaluateRule(User user);
}
