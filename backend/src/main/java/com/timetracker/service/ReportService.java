package com.timetracker.service;

import com.timetracker.dto.mapper.TimeEntryMapper;
import com.timetracker.dto.response.DailySummaryResponse;
import com.timetracker.dto.response.TimeEntryResponse;
import com.timetracker.dto.response.WeeklySummaryResponse;
import com.timetracker.entity.Category;
import com.timetracker.entity.Task;
import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import com.timetracker.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryMapper timeEntryMapper;
    private final UserService userService;
    private final ValidationService validationService;

    /**
     * Generate daily summary for a specific date
     */
    public DailySummaryResponse getDailySummary(LocalDate date) {
        User user = userService.getCurrentUser();
        List<TimeEntry> timeEntries = timeEntryRepository.findByUserAndEntryDateOrderByStartTimeAsc(user, date);

        DailySummaryResponse summary = new DailySummaryResponse();
        summary.setDate(date);
        summary.setTimeEntries(timeEntryMapper.toResponseList(timeEntries));

        if (timeEntries.isEmpty()) {
            summary.setTotalMinutes(0);
            summary.setTotalTimeFormatted("0m");
            summary.setTotalEntries(0);
            summary.setCategoryBreakdowns(new ArrayList<>());
            summary.setTaskBreakdowns(new ArrayList<>());
            summary.setWarnings(new ArrayList<>());
            return summary;
        }

        // Calculate totals
        int totalMinutes = timeEntries.stream()
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        summary.setTotalMinutes(totalMinutes);
        summary.setTotalTimeFormatted(formatMinutes(totalMinutes));
        summary.setTotalEntries(timeEntries.size());

        // Generate category breakdowns
        summary.setCategoryBreakdowns(generateCategoryBreakdowns(timeEntries));

        // Generate task breakdowns
        summary.setTaskBreakdowns(generateTaskBreakdowns(timeEntries));

        // Generate warnings using validation service
        var validation = validationService.validateDayEntries(user, date);
        summary.setWarnings(validation.getWarnings());

        return summary;
    }

    /**
     * Generate weekly summary
     */
    public WeeklySummaryResponse getWeeklySummary(LocalDate date) {
        // Get the start of the week (Monday)
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        User user = userService.getCurrentUser();
        List<TimeEntry> weekEntries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, weekStart, weekEnd);

        WeeklySummaryResponse summary = new WeeklySummaryResponse();
        summary.setWeekStartDate(weekStart);
        summary.setWeekEndDate(weekEnd);

        if (weekEntries.isEmpty()) {
            summary.setTotalMinutes(0);
            summary.setTotalTimeFormatted("0m");
            summary.setTotalEntries(0);
            summary.setAverageDailyHours(0.0);
            summary.setDailySummaries(new ArrayList<>());
            summary.setCategoryBreakdowns(new ArrayList<>());
            summary.setTaskBreakdowns(new ArrayList<>());
            return summary;
        }

        // Calculate weekly totals
        int totalMinutes = weekEntries.stream()
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        summary.setTotalMinutes(totalMinutes);
        summary.setTotalTimeFormatted(formatMinutes(totalMinutes));
        summary.setTotalEntries(weekEntries.size());

        // Calculate average daily hours
        double averageDailyHours = totalMinutes / 60.0 / 7.0;
        summary.setAverageDailyHours(Math.round(averageDailyHours * 100.0) / 100.0);

        // Generate daily summaries for each day of the week
        List<DailySummaryResponse> dailySummaries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.plusDays(i);
            dailySummaries.add(getDailySummary(currentDate));
        }
        summary.setDailySummaries(dailySummaries);

        // Generate weekly category and task breakdowns
        summary.setCategoryBreakdowns(generateCategoryBreakdowns(weekEntries));
        summary.setTaskBreakdowns(generateTaskBreakdowns(weekEntries));

        return summary;
    }

    /**
     * Get time tracking statistics for a date range
     */
    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> entries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        if (entries.isEmpty()) {
            stats.put("totalMinutes", 0);
            stats.put("totalEntries", 0);
            stats.put("averageEntryLength", 0);
            stats.put("mostUsedTask", null);
            stats.put("daysTracked", 0);
            return stats;
        }

        // Basic statistics
        int totalMinutes = entries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();
        stats.put("totalMinutes", totalMinutes);
        stats.put("totalTimeFormatted", formatMinutes(totalMinutes));
        stats.put("totalEntries", entries.size());

        // Average entry length
        double averageLength = (double) totalMinutes / entries.size();
        stats.put("averageEntryLength", Math.round(averageLength));
        stats.put("averageEntryLengthFormatted", formatMinutes((int) averageLength));

        // Most used task
        Map<Task, Integer> taskMinutes = entries.stream()
                .collect(Collectors.groupingBy(
                        TimeEntry::getTask,
                        Collectors.summingInt(TimeEntry::getDurationMinutes)
                ));

        Optional<Map.Entry<Task, Integer>> mostUsedTask = taskMinutes.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (mostUsedTask.isPresent()) {
            Task task = mostUsedTask.get().getKey();
            stats.put("mostUsedTask", Map.of(
                    "id", task.getId(),
                    "title", task.getTitle(),
                    "categoryTitle", task.getCategory().getTitle(),
                    "minutes", mostUsedTask.get().getValue(),
                    "timeFormatted", formatMinutes(mostUsedTask.get().getValue())
            ));
        }

        // Days tracked
        long daysTracked = entries.stream()
                .map(TimeEntry::getEntryDate)
                .distinct()
                .count();
        stats.put("daysTracked", daysTracked);

        // Average daily hours
        if (daysTracked > 0) {
            double averageDailyHours = totalMinutes / 60.0 / daysTracked;
            stats.put("averageDailyHours", Math.round(averageDailyHours * 100.0) / 100.0);
        }

        return stats;
    }

    /**
     * Get productivity insights for a user
     */
    public Map<String, Object> getProductivityInsights(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> entries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);

        Map<String, Object> insights = new HashMap<>();

        if (entries.isEmpty()) {
            insights.put("message", "No time entries found for the specified period");
            return insights;
        }

        // Group by date for daily analysis
        Map<LocalDate, List<TimeEntry>> entriesByDate = entries.stream()
                .collect(Collectors.groupingBy(TimeEntry::getEntryDate));

        // Most productive day
        Optional<Map.Entry<LocalDate, List<TimeEntry>>> mostProductiveDay = entriesByDate.entrySet().stream()
                .max(Comparator.comparing(entry ->
                        entry.getValue().stream().mapToInt(TimeEntry::getDurationMinutes).sum()));

        if (mostProductiveDay.isPresent()) {
            LocalDate date = mostProductiveDay.get().getKey();
            int minutes = mostProductiveDay.get().getValue().stream()
                    .mapToInt(TimeEntry::getDurationMinutes).sum();
            insights.put("mostProductiveDay", Map.of(
                    "date", date,
                    "minutes", minutes,
                    "timeFormatted", formatMinutes(minutes)
            ));
        }

        // Average session length by task
        Map<String, Double> avgSessionByTask = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getTask().getTitle(),
                        Collectors.averagingInt(TimeEntry::getDurationMinutes)
                ));

        insights.put("averageSessionByTask", avgSessionByTask.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutes(entry.getValue().intValue())
                )));

        // Time distribution analysis
        double totalHours = entries.stream().mapToInt(TimeEntry::getDurationMinutes).sum() / 60.0;
        Map<String, Double> categoryDistribution = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getTask().getCategory().getTitle(),
                        Collectors.summingDouble(entry -> entry.getDurationMinutes() / 60.0)
                ));

        Map<String, String> categoryPercentages = categoryDistribution.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.format("%.1f%%", (entry.getValue() / totalHours) * 100)
                ));

        insights.put("categoryDistribution", categoryPercentages);

        return insights;
    }

    // Private helper methods

    private List<DailySummaryResponse.CategoryTimeBreakdown> generateCategoryBreakdowns(List<TimeEntry> timeEntries) {
        Map<Category, List<TimeEntry>> categorizedEntries = timeEntries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getTask().getCategory()));

        return categorizedEntries.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<TimeEntry> entries = entry.getValue();
                    int totalMinutes = entries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();

                    DailySummaryResponse.CategoryTimeBreakdown breakdown =
                            new DailySummaryResponse.CategoryTimeBreakdown();
                    breakdown.setCategoryId(category.getId());
                    breakdown.setCategoryTitle(category.getTitle());
                    breakdown.setTotalMinutes(totalMinutes);
                    breakdown.setTimeFormatted(formatMinutes(totalMinutes));
                    breakdown.setEntryCount(entries.size());

                    return breakdown;
                })
                .sorted(Comparator.comparing(DailySummaryResponse.CategoryTimeBreakdown::getTotalMinutes).reversed())
                .collect(Collectors.toList());
    }

    private List<DailySummaryResponse.TaskTimeBreakdown> generateTaskBreakdowns(List<TimeEntry> timeEntries) {
        Map<Task, List<TimeEntry>> taskEntries = timeEntries.stream()
                .collect(Collectors.groupingBy(TimeEntry::getTask));

        return taskEntries.entrySet().stream()
                .map(entry -> {
                    Task task = entry.getKey();
                    List<TimeEntry> entries = entry.getValue();
                    int totalMinutes = entries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();

                    DailySummaryResponse.TaskTimeBreakdown breakdown =
                            new DailySummaryResponse.TaskTimeBreakdown();
                    breakdown.setTaskId(task.getId());
                    breakdown.setTaskTitle(task.getTitle());
                    breakdown.setTaskColor(task.getColor());
                    breakdown.setTaskIcon(task.getIcon());
                    breakdown.setCategoryTitle(task.getCategory().getTitle());
                    breakdown.setTotalMinutes(totalMinutes);
                    breakdown.setTimeFormatted(formatMinutes(totalMinutes));
                    breakdown.setEntryCount(entries.size());

                    return breakdown;
                })
                .sorted(Comparator.comparing(DailySummaryResponse.TaskTimeBreakdown::getTotalMinutes).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get enhanced statistics with additional monthly insights
     */
    public Map<String, Object> getEnhancedStatistics(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> entries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);

        Map<String, Object> stats = getStatistics(startDate, endDate);

        if (entries.isEmpty()) {
            return stats;
        }

        // Add category breakdown with enhanced info
        List<Map<String, Object>> categoryBreakdown = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getTask().getCategory()))
                .entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<TimeEntry> categoryEntries = entry.getValue();
                    int totalMinutes = categoryEntries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();

                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("categoryId", category.getId());
                    categoryData.put("categoryTitle", category.getTitle());
                    categoryData.put("totalMinutes", totalMinutes);
                    categoryData.put("timeFormatted", formatMinutes(totalMinutes));
                    categoryData.put("entryCount", categoryEntries.size());

                    return categoryData;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalMinutes"), (Integer) a.get("totalMinutes")))
                .collect(Collectors.toList());

        stats.put("categoryBreakdown", categoryBreakdown);

        // Add task breakdown with enhanced info
        List<Map<String, Object>> taskBreakdown = entries.stream()
                .collect(Collectors.groupingBy(TimeEntry::getTask))
                .entrySet().stream()
                .map(entry -> {
                    Task task = entry.getKey();
                    List<TimeEntry> taskEntries = entry.getValue();
                    int totalMinutes = taskEntries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();

                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("taskId", task.getId());
                    taskData.put("taskTitle", task.getTitle());
                    taskData.put("taskColor", task.getColor());
                    taskData.put("categoryTitle", task.getCategory().getTitle());
                    taskData.put("totalMinutes", totalMinutes);
                    taskData.put("timeFormatted", formatMinutes(totalMinutes));
                    taskData.put("entryCount", taskEntries.size());

                    return taskData;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalMinutes"), (Integer) a.get("totalMinutes")))
                .collect(Collectors.toList());

        stats.put("taskBreakdown", taskBreakdown);

        // Add weekday vs weekend breakdown
        int weekdayMinutes = 0;
        int weekendMinutes = 0;

        for (TimeEntry entry : entries) {
            DayOfWeek dayOfWeek = entry.getEntryDate().getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                weekendMinutes += entry.getDurationMinutes();
            } else {
                weekdayMinutes += entry.getDurationMinutes();
            }
        }

        stats.put("weekdayMinutes", weekdayMinutes);
        stats.put("weekendMinutes", weekendMinutes);

        // Add session statistics
        OptionalDouble avgSession = entries.stream().mapToInt(TimeEntry::getDurationMinutes).average();
        int longestSession = entries.stream().mapToInt(TimeEntry::getDurationMinutes).max().orElse(0);

        stats.put("averageSession", avgSession.orElse(0.0));
        stats.put("longestSession", longestSession);

        return stats;
    }

    private String formatMinutes(int minutes) {
        if (minutes == 0) {
            return "0m";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "m";
        } else if (remainingMinutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + remainingMinutes + "m";
        }
    }
}