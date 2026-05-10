package com.firstclub.membership.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BenefitType {

    FREE_DELIVERY("Free Delivery"),
    DISCOUNTS("Discounts"),
    EARLY_ACCESS("Early Access");

    private final String displayName;
}
