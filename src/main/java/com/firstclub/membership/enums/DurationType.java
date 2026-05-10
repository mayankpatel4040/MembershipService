package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DurationType {
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    ANNUALLY("Annually", 365);

    private final String displayName;
    private final int days;
}
