package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

public class NoActiveSubscriptionException extends MembershipException {
    public NoActiveSubscriptionException() {
        super("No active subscription found for user.", HttpStatus.NOT_FOUND);
    }
}
