package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

public class MembershipException extends RuntimeException {

    private final HttpStatus status;

    public MembershipException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
