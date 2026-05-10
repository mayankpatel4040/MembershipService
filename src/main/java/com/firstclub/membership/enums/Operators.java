package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Operators {
    EQUALS("equals"),
    NOT_EQUALS("not_equals"),
    GREATER_THAN("greater_than"),
    LESS_THAN("less_than"),
    GREATER_THAN_OR_EQUALS("greater_than_or_equals"),
    LESS_THAN_OR_EQUALS("less_than_or_equals"),
    IN("in"),
    NOT_IN("not_in");

    private final String displayName;
}
