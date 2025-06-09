package com.timetracker.service;

import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import com.timetracker.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserService userService;

    /**
     * Export time entries to CSV format for a date range
     */
    public ResponseEntity<byte[]> exportTimeEntriesToCsv(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> timeEntries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);

        try {
            byte[] csvData = generateCsvData(timeEntries);

            String filename = String.format("time_entries_%s_to_%s.csv",
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            log.info("Exported {} time entries to CSV for user {} (date range: {} to {})",
                    timeEntries.size(), user.getEmail(), startDate, endDate);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            log.error("Error exporting time entries to CSV for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to export time entries to CSV", e);
        }
    }

    /**
     * Export daily summary to CSV format
     */
    public ResponseEntity<byte[]> exportDailySummaryToCsv(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        try {
            byte[] csvData = generateDailySummaryCsvData(user, startDate, endDate);

            String filename = String.format("daily_summary_%s_to_%s.csv",
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            log.info("Exported daily summary to CSV for user {} (date range: {} to {})",
                    user.getEmail(), startDate, endDate);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            log.error("Error exporting daily summary to CSV for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to export daily summary to CSV", e);
        }
    }

    /**
     * Export task summary to CSV format
     */
    public ResponseEntity<byte[]> exportTaskSummaryToCsv(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> timeEntries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);

        try {
            byte[] csvData = generateTaskSummaryCsvData(timeEntries);

            String filename = String.format("task_summary_%s_to_%s.csv",
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            log.info("Exported task summary to CSV for user {} (date range: {} to {})",
                    user.getEmail(), startDate, endDate);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            log.error("Error exporting task summary to CSV for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to export task summary to CSV", e);
        }
    }

    // Private helper methods

    private byte[] generateCsvData(List<TimeEntry> timeEntries) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        // Write CSV header
        writer.println("Date,Start Time,End Time,Duration (minutes),Duration (formatted),Task,Category,Description,Billable");

        // Write data rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (TimeEntry entry : timeEntries) {
            String date = entry.getEntryDate().format(dateFormatter);
            String startTime = entry.getStartTime().format(timeFormatter);
            String endTime = entry.getEndTime().format(timeFormatter);
            int durationMinutes = entry.getDurationMinutes();
            String durationFormatted = formatMinutes(durationMinutes);
            String taskTitle = escapeCsvValue(entry.getTask().getTitle());
            String categoryTitle = escapeCsvValue(entry.getTask().getCategory().getTitle());
            String description = escapeCsvValue(entry.getDescription() != null ? entry.getDescription() : "");
            String billable = entry.getIsBillable() != null && entry.getIsBillable() ? "Yes" : "No";

            writer.printf("%s,%s,%s,%d,%s,%s,%s,%s,%s%n",
                    date, startTime, endTime, durationMinutes, durationFormatted,
                    taskTitle, categoryTitle, description, billable);
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    private byte[] generateDailySummaryCsvData(User user, LocalDate startDate, LocalDate endDate) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        // Write CSV header
        writer.println("Date,Total Minutes,Total Time (formatted),Number of Entries,Categories Worked,Most Used Task");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Generate daily summaries
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<TimeEntry> dayEntries = timeEntryRepository.findByUserAndEntryDateOrderByStartTimeAsc(user, currentDate);

            String date = currentDate.format(dateFormatter);
            int totalMinutes = dayEntries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();
            String totalTimeFormatted = formatMinutes(totalMinutes);
            int entryCount = dayEntries.size();

            long categoriesWorked = dayEntries.stream()
                    .map(entry -> entry.getTask().getCategory().getTitle())
                    .distinct()
                    .count();

            String mostUsedTask = dayEntries.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            entry -> entry.getTask().getTitle(),
                            java.util.stream.Collectors.summingInt(TimeEntry::getDurationMinutes)
                    ))
                    .entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .map(entry -> escapeCsvValue(entry.getKey()))
                    .orElse("");

            writer.printf("%s,%d,%s,%d,%d,%s%n",
                    date, totalMinutes, totalTimeFormatted, entryCount, categoriesWorked, mostUsedTask);

            currentDate = currentDate.plusDays(1);
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    private byte[] generateTaskSummaryCsvData(List<TimeEntry> timeEntries) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        // Write CSV header
        writer.println("Task,Category,Total Minutes,Total Time (formatted),Entry Count,Average Session Length,Color,Icon");

        // Group by task and calculate summaries
        var taskSummaries = timeEntries.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        TimeEntry::getTask,
                        java.util.stream.Collectors.toList()
                ));

        // Sort by total time descending
        taskSummaries.entrySet().stream()
                .sorted((a, b) -> {
                    int totalA = a.getValue().stream().mapToInt(TimeEntry::getDurationMinutes).sum();
                    int totalB = b.getValue().stream().mapToInt(TimeEntry::getDurationMinutes).sum();
                    return Integer.compare(totalB, totalA);
                })
                .forEach(entry -> {
                    var task = entry.getKey();
                    var entries = entry.getValue();

                    String taskTitle = escapeCsvValue(task.getTitle());
                    String categoryTitle = escapeCsvValue(task.getCategory().getTitle());
                    int totalMinutes = entries.stream().mapToInt(TimeEntry::getDurationMinutes).sum();
                    String totalTimeFormatted = formatMinutes(totalMinutes);
                    int entryCount = entries.size();
                    int averageSessionLength = totalMinutes / entryCount;
                    String averageSessionFormatted = formatMinutes(averageSessionLength);
                    String color = task.getColor();
                    String icon = task.getIcon() != null ? task.getIcon() : "";

                    writer.printf("%s,%s,%d,%s,%d,%s,%s,%s%n",
                            taskTitle, categoryTitle, totalMinutes, totalTimeFormatted,
                            entryCount, averageSessionFormatted, color, icon);
                });

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If the value contains comma, quote, or newline, wrap it in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
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