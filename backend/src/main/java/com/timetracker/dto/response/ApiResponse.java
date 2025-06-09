package com.timetracker.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Instant timestamp;

    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public ApiResponse(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }
}