package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class WeeklySummaryResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Integer totalMinutes;
    private String totalTimeFormatted;
    private Integer totalEntries;
    private Double averageDailyHours;

    // Daily summaries for the week
    private List<DailySummaryResponse> dailySummaries;

    // Weekly breakdowns
    private List<DailySummaryResponse.CategoryTimeBreakdown> categoryBreakdowns;
    private List<DailySummaryResponse.TaskTimeBreakdown> taskBreakdowns;
}