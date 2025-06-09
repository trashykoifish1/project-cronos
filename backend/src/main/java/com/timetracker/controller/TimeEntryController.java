package com.timetracker.controller;

import com.timetracker.dto.request.BulkTimeEntryRequest;
import com.timetracker.dto.request.TimeEntryCreateRequest;
import com.timetracker.dto.request.TimeEntryUpdateRequest;
import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.BulkOperationResponse;
import com.timetracker.dto.response.TimeEntryResponse;
import com.timetracker.dto.response.TimeEntryValidationResponse;
import com.timetracker.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Time Entries", description = "Time tracking operations")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping("/date/{date}")
    @Operation(summary = "Get time entries for date", description = "Retrieve all time entries for a specific date")
    public ResponseEntity<ApiResponse<List<TimeEntryResponse>>> getTimeEntriesForDate(
            @Parameter(description = "Date (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeEntryResponse> timeEntries = timeEntryService.getTimeEntriesForDate(date);
        return ResponseEntity.ok(ApiResponse.success(timeEntries));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get time entries for date range", description = "Retrieve time entries for a date range")
    public ResponseEntity<ApiResponse<List<TimeEntryResponse>>> getTimeEntriesForDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TimeEntryResponse> timeEntries = timeEntryService.getTimeEntriesForDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(timeEntries));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time entry by ID", description = "Retrieve a specific time entry by its ID")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> getTimeEntryById(
            @Parameter(description = "Time entry ID") @PathVariable Long id) {
        TimeEntryResponse timeEntry = timeEntryService.getTimeEntryById(id);
        return ResponseEntity.ok(ApiResponse.success(timeEntry));
    }

    @PostMapping
    @Operation(summary = "Create time entry", description = "Create a new time entry with validation")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> createTimeEntry(
            @Valid @RequestBody TimeEntryCreateRequest request) {
        TimeEntryResponse timeEntry = timeEntryService.createTimeEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time entry created successfully", timeEntry));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time entry", description = "Update an existing time entry")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> updateTimeEntry(
            @Parameter(description = "Time entry ID") @PathVariable Long id,
            @Valid @RequestBody TimeEntryUpdateRequest request) {
        TimeEntryResponse timeEntry = timeEntryService.updateTimeEntry(id, request);
        return ResponseEntity.ok(ApiResponse.success("Time entry updated successfully", timeEntry));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete time entry", description = "Delete a time entry")
    public ResponseEntity<ApiResponse<String>> deleteTimeEntry(
            @Parameter(description = "Time entry ID") @PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.ok(ApiResponse.success("Time entry deleted successfully"));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create time entries", description = "Create multiple time entries for a specific date")
    public ResponseEntity<ApiResponse<BulkOperationResponse>> bulkCreateTimeEntries(
            @Valid @RequestBody BulkTimeEntryRequest request) {
        BulkOperationResponse result = timeEntryService.bulkCreateTimeEntries(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk time entries processed", result));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate time entry", description = "Validate a time entry for conflicts and business rules")
    public ResponseEntity<ApiResponse<TimeEntryValidationResponse>> validateTimeEntry(
            @Valid @RequestBody TimeEntryCreateRequest request) {
        TimeEntryValidationResponse validation = timeEntryService.validateTimeEntry(request);
        return ResponseEntity.ok(ApiResponse.success(validation));
    }

    @GetMapping("/daily-total/{date}")
    @Operation(summary = "Get daily total minutes", description = "Get total minutes tracked for a specific date")
    public ResponseEntity<ApiResponse<Integer>> getDailyTotalMinutes(
            @Parameter(description = "Date (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Integer totalMinutes = timeEntryService.getDailyTotalMinutes(date);
        return ResponseEntity.ok(ApiResponse.success(totalMinutes));
    }

    @GetMapping("/overlaps")
    @Operation(summary = "Check for overlapping entries", description = "Find time entries that overlap with the specified time range")
    public ResponseEntity<ApiResponse<List<TimeEntryResponse>>> findOverlappingEntries(
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start time (HH:mm)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @Parameter(description = "End time (HH:mm)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @Parameter(description = "Time entry ID to exclude from check")
            @RequestParam(required = false) Long excludeId) {
        List<TimeEntryResponse> overlapping = timeEntryService.findOverlappingEntries(date, startTime, endTime, excludeId);
        return ResponseEntity.ok(ApiResponse.success(overlapping));
    }
}