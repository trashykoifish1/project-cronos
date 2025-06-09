package com.timetracker.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class ValidationErrorResponse {
    private String error = "VALIDATION_ERROR";
    private String message;
    private Map<String, List<String>> fieldErrors;
    private Instant timestamp;
    private String path;

    public ValidationErrorResponse(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}