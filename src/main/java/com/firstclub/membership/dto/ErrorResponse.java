package com.firstclub.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private ZonedDateTime timestamp;

    public ErrorResponse(String message, int status) {
        this(message, status, ZonedDateTime.now());
    }
}
