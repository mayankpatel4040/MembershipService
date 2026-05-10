package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

public class DuplicateSubscriptionException extends MembershipException {
    public DuplicateSubscriptionException() {
        super("User already has an active subscription. Use /changePlan to upgrade or downgrade.", HttpStatus.CONFLICT);
    }
}
