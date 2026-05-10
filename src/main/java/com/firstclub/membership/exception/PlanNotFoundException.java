package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends MembershipException {
    public PlanNotFoundException(String planName) {
        super("Active membership plan not found: " + planName, HttpStatus.NOT_FOUND);
    }
}
