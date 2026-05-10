package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CohortType {
    GOOD("Good"),
    BAD("Bad"),
    MEDIUM("Medium");

    private final String displayName;
}
