package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CriteriaType {
    ORDER_COUNT("Order Count"),
    ORDER_VALUE("Order Value"),
    USER_COHORT("User Cohort");

    private final String displayName;

}
