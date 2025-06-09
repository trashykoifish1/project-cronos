package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class DailySummaryResponse {

    private LocalDate date;
    private Integer totalMinutes;
    private String totalTimeFormatted; // e.g., "8h 30m"
    private Integer totalEntries;

    // Time breakdown by category
    private List<CategoryTimeBreakdown> categoryBreakdowns;

    // Time breakdown by task
    private List<TaskTimeBreakdown> taskBreakdowns;

    // All time entries for the day
    private List<TimeEntryResponse> timeEntries;

    // Validation warnings
    private List<String> warnings;

    @Data
    public static class CategoryTimeBreakdown {
        private Long categoryId;
        private String categoryTitle;
        private Integer totalMinutes;
        private String timeFormatted;
        private Integer entryCount;
    }

    @Data
    public static class TaskTimeBreakdown {
        private Long taskId;
        private String taskTitle;
        private String taskColor;
        private String taskIcon;
        private String categoryTitle;
        private Integer totalMinutes;
        private String timeFormatted;
        private Integer entryCount;
    }
}