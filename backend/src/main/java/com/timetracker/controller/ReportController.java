package com.timetracker.controller;

import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.DailySummaryResponse;
import com.timetracker.dto.response.WeeklySummaryResponse;
import com.timetracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Analytics and reporting operations")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily/{date}")
    @Operation(summary = "Get daily summary", description = "Get comprehensive daily time tracking summary")
    public ResponseEntity<ApiResponse<DailySummaryResponse>> getDailySummary(
            @Parameter(description = "Date (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySummaryResponse summary = reportService.getDailySummary(date);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/weekly/{date}")
    @Operation(summary = "Get weekly summary", description = "Get weekly time tracking summary (Monday to Sunday)")
    public ResponseEntity<ApiResponse<WeeklySummaryResponse>> getWeeklySummary(
            @Parameter(description = "Any date within the target week (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        WeeklySummaryResponse summary = reportService.getWeeklySummary(date);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get time tracking statistics", description = "Get statistical analysis for a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> statistics = reportService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/enhanced-statistics")
    @Operation(summary = "Get enhanced statistics", description = "Get enhanced statistics with category and task breakdowns for a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEnhancedStatistics(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> statistics = reportService.getEnhancedStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/productivity-insights")
    @Operation(summary = "Get productivity insights", description = "Get productivity analysis and insights for a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductivityInsights(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> insights = reportService.getProductivityInsights(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(insights));
    }

    @GetMapping("/daily-range")
    @Operation(summary = "Get daily summaries for range", description = "Get daily summaries for each day in a date range")
    public ResponseEntity<ApiResponse<Map<String, DailySummaryResponse>>> getDailySummariesForRange(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, DailySummaryResponse> summaries = new java.util.LinkedHashMap<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            DailySummaryResponse dailySummary = reportService.getDailySummary(currentDate);
            summaries.put(currentDate.toString(), dailySummary);
            currentDate = currentDate.plusDays(1);
        }

        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @GetMapping("/current-week")
    @Operation(summary = "Get current week summary", description = "Get summary for the current week")
    public ResponseEntity<ApiResponse<WeeklySummaryResponse>> getCurrentWeekSummary() {
        LocalDate today = LocalDate.now();
        WeeklySummaryResponse summary = reportService.getWeeklySummary(today);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/last-7-days")
    @Operation(summary = "Get last 7 days statistics", description = "Get statistics for the last 7 days")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLast7DaysStatistics() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Last 7 days including today
        Map<String, Object> statistics = reportService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/last-30-days")
    @Operation(summary = "Get last 30 days statistics", description = "Get statistics for the last 30 days")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLast30DaysStatistics() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29); // Last 30 days including today
        Map<String, Object> statistics = reportService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

}