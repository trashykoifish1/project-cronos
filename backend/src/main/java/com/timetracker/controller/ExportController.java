package com.timetracker.controller;

import com.timetracker.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Data export operations")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/time-entries/csv")
    @Operation(summary = "Export time entries to CSV", description = "Export time entries for a date range to CSV format")
    public ResponseEntity<byte[]> exportTimeEntriesToCsv(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return exportService.exportTimeEntriesToCsv(startDate, endDate);
    }

    @GetMapping("/daily-summary/csv")
    @Operation(summary = "Export daily summary to CSV", description = "Export daily summaries for a date range to CSV format")
    public ResponseEntity<byte[]> exportDailySummaryToCsv(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return exportService.exportDailySummaryToCsv(startDate, endDate);
    }

    @GetMapping("/task-summary/csv")
    @Operation(summary = "Export task summary to CSV", description = "Export task summaries for a date range to CSV format")
    public ResponseEntity<byte[]> exportTaskSummaryToCsv(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return exportService.exportTaskSummaryToCsv(startDate, endDate);
    }

    @GetMapping("/current-week/csv")
    @Operation(summary = "Export current week to CSV", description = "Export current week's time entries to CSV")
    public ResponseEntity<byte[]> exportCurrentWeekToCsv() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        return exportService.exportTimeEntriesToCsv(weekStart, weekEnd);
    }

    @GetMapping("/last-30-days/csv")
    @Operation(summary = "Export last 30 days to CSV", description = "Export last 30 days' time entries to CSV")
    public ResponseEntity<byte[]> exportLast30DaysToCsv() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        return exportService.exportTimeEntriesToCsv(startDate, endDate);
    }

    @GetMapping("/month/{year}/{month}/csv")
    @Operation(summary = "Export specific month to CSV", description = "Export a specific month's time entries to CSV")
    public ResponseEntity<byte[]> exportMonthToCsv(
            @Parameter(description = "Year (e.g., 2024)") @PathVariable int year,
            @Parameter(description = "Month (1-12)") @PathVariable int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return exportService.exportTimeEntriesToCsv(startDate, endDate);
    }
}