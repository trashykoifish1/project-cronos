package com.timetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private Object details;
    private String errorId;
    private Instant timestamp;
    private String path;
}