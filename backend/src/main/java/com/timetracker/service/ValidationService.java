package com.timetracker.service;

import com.timetracker.dto.response.TimeEntryValidationResponse;
import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import com.timetracker.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ValidationService {

    private final TimeEntryRepository timeEntryRepository;

    /**
     * Validate a day's time entries for unusual patterns and potential issues
     */
    public TimeEntryValidationResponse validateDayEntries(User user, LocalDate date) {
        TimeEntryValidationResponse response = new TimeEntryValidationResponse();
        response.setDate(date);
        response.setValid(true);
        response.setWarnings(new ArrayList<>());
        response.setErrors(new ArrayList<>());
        response.setConflicts(new ArrayList<>());

        List<TimeEntry> dayEntries = timeEntryRepository.findByUserAndEntryDateOrderByStartTimeAsc(user, date);

        if (dayEntries.isEmpty()) {
            return response; // No entries to validate
        }

        // Calculate total minutes
        int totalMinutes = dayEntries.stream()
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        // Check for excessive daily hours
        if (totalMinutes > 16 * 60) { // More than 16 hours
            response.getWarnings().add("Daily total exceeds 16 hours (" + formatMinutes(totalMinutes) + ")");
        }

        if (totalMinutes > 24 * 60) { // More than 24 hours (impossible)
            response.getErrors().add("Daily total exceeds 24 hours (" + formatMinutes(totalMinutes) + ")");
            response.setValid(false);
        }

        // Check for overlapping entries (shouldn't happen with DB constraints, but good to verify)
        validateNoOverlaps(dayEntries, response);

        // Check for unusually long single entries
        validateEntryDurations(dayEntries, response);

        // Check for unusual gaps or patterns
        validateTimePatterns(dayEntries, response);

        // Check for midnight spanning entries
        validateMidnightSpanning(dayEntries, response);

        return response;
    }

    /**
     * Validate that entries don't overlap
     */
    private void validateNoOverlaps(List<TimeEntry> entries, TimeEntryValidationResponse response) {
        for (int i = 0; i < entries.size() - 1; i++) {
            TimeEntry current = entries.get(i);
            TimeEntry next = entries.get(i + 1);

            if (current.getEndTime().isAfter(next.getStartTime())) {
                TimeEntryValidationResponse.ConflictingEntry conflict =
                        new TimeEntryValidationResponse.ConflictingEntry();
                conflict.setTimeEntryId(current.getId());
                conflict.setTaskTitle(current.getTask().getTitle());
                conflict.setStartTime(current.getStartTime().toString());
                conflict.setEndTime(current.getEndTime().toString());
                conflict.setConflictType("OVERLAP");
                response.getConflicts().add(conflict);

                response.getErrors().add("Time entries overlap: " +
                        current.getTask().getTitle() + " and " + next.getTask().getTitle());
                response.setValid(false);
            }
        }
    }

    /**
     * Validate individual entry durations
     */
    private void validateEntryDurations(List<TimeEntry> entries, TimeEntryValidationResponse response) {
        for (TimeEntry entry : entries) {
            // Warning for very long entries
            if (entry.getDurationMinutes() > 12 * 60) { // More than 12 hours
                response.getWarnings().add("Long time entry detected: " +
                        entry.getTask().getTitle() + " (" + formatMinutes(entry.getDurationMinutes()) + ")");
            }

            // Warning for very short entries
            if (entry.getDurationMinutes() < 15) {
                response.getWarnings().add("Short time entry detected: " +
                        entry.getTask().getTitle() + " (" + formatMinutes(entry.getDurationMinutes()) + ")");
            }
        }
    }

    /**
     * Validate time patterns and gaps
     */
    private void validateTimePatterns(List<TimeEntry> entries, TimeEntryValidationResponse response) {
        if (entries.size() < 2) {
            return; // Need at least 2 entries to check patterns
        }

        // Check for large gaps between entries
        for (int i = 0; i < entries.size() - 1; i++) {
            TimeEntry current = entries.get(i);
            TimeEntry next = entries.get(i + 1);

            long gapMinutes = java.time.Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

            if (gapMinutes > 4 * 60) { // More than 4 hours gap
                response.getWarnings().add("Large gap detected between entries: " +
                        formatMinutes((int) gapMinutes) + " between " +
                        current.getTask().getTitle() + " and " + next.getTask().getTitle());
            }
        }

        // Check for entries starting very early or ending very late
        TimeEntry firstEntry = entries.get(0);
        TimeEntry lastEntry = entries.get(entries.size() - 1);

        if (firstEntry.getStartTime().isBefore(LocalTime.of(5, 0))) {
            response.getWarnings().add("Very early start time: " + firstEntry.getStartTime());
        }

        if (lastEntry.getEndTime().isAfter(LocalTime.of(23, 0))) {
            response.getWarnings().add("Very late end time: " + lastEntry.getEndTime());
        }
    }

    /**
     * Check for entries that might span midnight (which should be split into separate days)
     */
    private void validateMidnightSpanning(List<TimeEntry> entries, TimeEntryValidationResponse response) {
        for (TimeEntry entry : entries) {
            // Check if entry ends on or after midnight (next day)
            if (entry.getEndTime().isBefore(entry.getStartTime())) {
                response.getWarnings().add("Time entry may span midnight: " +
                        entry.getTask().getTitle() + " (" +
                        entry.getStartTime() + " to " + entry.getEndTime() + ")");
            }

            // Check for entries that start very late and end very early (suggesting midnight span)
            if (entry.getStartTime().isAfter(LocalTime.of(23, 0)) &&
                    entry.getEndTime().isBefore(LocalTime.of(6, 0))) {
                response.getWarnings().add("Possible midnight-spanning entry: " +
                        entry.getTask().getTitle());
            }
        }
    }

    /**
     * Format minutes into human-readable format (e.g., "8h 30m")
     */
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

    /**
     * Validate time format and range
     */
    public boolean isValidTimeRange(LocalTime startTime, LocalTime endTime) {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /**
     * Validate minimum duration
     */
    public boolean isValidDuration(LocalTime startTime, LocalTime endTime, int minimumMinutes) {
        if (!isValidTimeRange(startTime, endTime)) {
            return false;
        }

        long duration = java.time.Duration.between(startTime, endTime).toMinutes();
        return duration >= minimumMinutes;
    }

    /**
     * Check if a color is a valid hex color
     */
    public boolean isValidHexColor(String color) {
        if (color == null || color.length() != 7 || !color.startsWith("#")) {
            return false;
        }

        try {
            Integer.parseInt(color.substring(1), 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}