package com.timetracker.exception;

import com.timetracker.dto.response.ErrorResponse;
import com.timetracker.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error("RESOURCE_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation error: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        ValidationErrorResponse response = new ValidationErrorResponse(ex.getMessage());
        response.setFieldErrors(ex.getFieldErrors());
        response.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Method argument validation failed: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        Map<String, List<String>> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new java.util.ArrayList<>()).add(errorMessage);
        });

        ValidationErrorResponse response = new ValidationErrorResponse("Validation failed for one or more fields");
        response.setFieldErrors(fieldErrors);
        response.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(TimeEntryOverlapException.class)
    public ResponseEntity<ErrorResponse> handleTimeOverlap(TimeEntryOverlapException ex, HttpServletRequest request) {
        log.warn("Time entry overlap: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        Map<String, Object> details = new HashMap<>();
        details.put("conflictingEntries", ex.getConflictingEntries().stream()
                .map(entry -> Map.of(
                        "id", entry.getId(),
                        "taskTitle", entry.getTask().getTitle(),
                        "startTime", entry.getStartTime().toString(),
                        "endTime", entry.getEndTime().toString()
                ))
                .collect(Collectors.toList()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .error("TIME_OVERLAP")
                        .message(ex.getMessage())
                        .details(details)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(CategoryArchivedException.class)
    public ResponseEntity<ErrorResponse> handleCategoryArchived(CategoryArchivedException ex, HttpServletRequest request) {
        log.warn("Category archived error: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("CATEGORY_ARCHIVED")
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleDailyLimitExceeded(DailyLimitExceededException ex, HttpServletRequest request) {
        log.warn("Daily limit exceeded: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        Map<String, Object> details = new HashMap<>();
        details.put("maxHours", ex.getMaxHours());
        details.put("actualHours", ex.getActualHours());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("DAILY_LIMIT_EXCEEDED")
                        .message(ex.getMessage())
                        .details(details)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {} - URL: {}", ex.getMessage(), request.getRequestURL());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("INVALID_ARGUMENT")
                        .message("Invalid request parameters: " + ex.getMessage())
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Runtime error [{}]: {} - URL: {}", errorId, ex.getMessage(), request.getRequestURL(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .error("RUNTIME_ERROR")
                        .message("An error occurred while processing your request")
                        .errorId(errorId)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]: {} - URL: {}", errorId, ex.getMessage(), request.getRequestURL(), ex);

        // Don't expose internal error details to users
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .error("INTERNAL_ERROR")
                        .message("Something went wrong. Please try again.")
                        .errorId(errorId)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }
}