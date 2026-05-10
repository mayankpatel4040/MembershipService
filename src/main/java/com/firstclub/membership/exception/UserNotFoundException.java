package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends MembershipException {
    public UserNotFoundException(String phoneNumber) {
        super("User not found with phone number: " + phoneNumber, HttpStatus.NOT_FOUND);
    }
}
