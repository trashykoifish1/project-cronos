package com.timetracker.service;

import com.timetracker.dto.mapper.TimeEntryMapper;
import com.timetracker.dto.request.BulkTimeEntryRequest;
import com.timetracker.dto.request.TimeEntryCreateRequest;
import com.timetracker.dto.request.TimeEntryUpdateRequest;
import com.timetracker.dto.response.BulkOperationResponse;
import com.timetracker.dto.response.TimeEntryResponse;
import com.timetracker.dto.response.TimeEntryValidationResponse;
import com.timetracker.entity.Task;
import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import com.timetracker.exception.ResourceNotFoundException;
import com.timetracker.exception.ValidationException;
import com.timetracker.repository.TaskRepository;
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
@Transactional
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;
    private final UserService userService;
    private final ValidationService validationService;

    /**
     * Get all time entries for a specific date
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getTimeEntriesForDate(LocalDate date) {
        User user = userService.getCurrentUser();
        List<TimeEntry> timeEntries = timeEntryRepository.findByUserAndEntryDateOrderByStartTimeAsc(user, date);
        return timeEntryMapper.toResponseList(timeEntries);
    }

    /**
     * Get time entries for a date range
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getTimeEntriesForDateRange(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<TimeEntry> timeEntries = timeEntryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
                user, startDate, endDate);
        return timeEntryMapper.toResponseList(timeEntries);
    }

    /**
     * Get time entry by ID
     */
    @Transactional(readOnly = true)
    public TimeEntryResponse getTimeEntryById(Long id) {
        TimeEntry timeEntry = findTimeEntryByIdAndUser(id);
        return timeEntryMapper.toTimeEntryResponse(timeEntry);
    }

    /**
     * Create a new time entry with validation
     */
    public TimeEntryResponse createTimeEntry(TimeEntryCreateRequest request) {
        User user = userService.getCurrentUser();
        Task task = findTaskByIdAndUser(request.getTaskId());

        // Validate the time entry
        validateTimeEntry(request, user, null);

        TimeEntry timeEntry = timeEntryMapper.toEntity(request);
        timeEntry.setUser(user);
        timeEntry.setTask(task);
        timeEntry.calculateDurationMinutes();

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);
        log.info("Created time entry for task '{}' on {} from {} to {} for user {}",
                task.getTitle(), request.getEntryDate(), request.getStartTime(),
                request.getEndTime(), user.getEmail());

        return timeEntryMapper.toTimeEntryResponse(savedTimeEntry);
    }

    /**
     * Update an existing time entry
     */
    public TimeEntryResponse updateTimeEntry(Long id, TimeEntryUpdateRequest request) {
        User user = userService.getCurrentUser();
        TimeEntry timeEntry = findTimeEntryByIdAndUser(id);
        Task task = findTaskByIdAndUser(request.getTaskId());

        // Validate the updated time entry
        validateTimeEntry(request, user, id);

        timeEntryMapper.updateEntityFromRequest(request, timeEntry);
        timeEntry.setTask(task);
        timeEntry.calculateDurationMinutes();

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);
        log.info("Updated time entry {} for user {}", id, user.getEmail());

        return timeEntryMapper.toTimeEntryResponse(savedTimeEntry);
    }

    /**
     * Delete a time entry
     */
    public void deleteTimeEntry(Long id) {
        User user = userService.getCurrentUser();
        TimeEntry timeEntry = findTimeEntryByIdAndUser(id);

        timeEntryRepository.delete(timeEntry);
        log.info("Deleted time entry {} for user {}", id, user.getEmail());
    }

    /**
     * Bulk create/update time entries for a specific date
     */
    public BulkOperationResponse bulkCreateTimeEntries(BulkTimeEntryRequest request) {
        User user = userService.getCurrentUser();
        BulkOperationResponse response = new BulkOperationResponse();
        response.setErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setCreatedIds(new ArrayList<>());
        response.setSkippedIds(new ArrayList<>());

        // If replaceExisting is true, delete all existing entries for the date
        if (Boolean.TRUE.equals(request.getReplaceExisting())) {
            List<TimeEntry> existingEntries = timeEntryRepository.findByUserAndEntryDateOrderByStartTimeAsc(
                    user, request.getEntryDate());
            timeEntryRepository.deleteAll(existingEntries);
            log.info("Deleted {} existing time entries for date {} for user {}",
                    existingEntries.size(), request.getEntryDate(), user.getEmail());
        }

        int successCount = 0;
        int failureCount = 0;

        for (TimeEntryCreateRequest entryRequest : request.getTimeEntries()) {
            try {
                entryRequest.setEntryDate(request.getEntryDate()); // Ensure date consistency

                // Validate individual entry
                validateTimeEntry(entryRequest, user, null);

                Task task = findTaskByIdAndUser(entryRequest.getTaskId());
                TimeEntry timeEntry = timeEntryMapper.toEntity(entryRequest);
                timeEntry.setUser(user);
                timeEntry.setTask(task);
                timeEntry.calculateDurationMinutes();

                TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
                response.getCreatedIds().add(savedEntry.getId());
                successCount++;

            } catch (Exception e) {
                response.getErrors().add("Failed to create time entry: " + e.getMessage());
                failureCount++;
            }
        }

        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        response.setTotalProcessed(request.getTimeEntries().size());

        log.info("Bulk created {} time entries ({} success, {} failure) for date {} for user {}",
                response.getTotalProcessed(), successCount, failureCount, request.getEntryDate(), user.getEmail());

        return response;
    }

    /**
     * Validate a time entry for conflicts and business rules
     */
    @Transactional(readOnly = true)
    public TimeEntryValidationResponse validateTimeEntry(TimeEntryCreateRequest request) {
        User user = userService.getCurrentUser();
        return validateTimeEntry(request, user, null);
    }

    /**
     * Get daily total minutes for a user and date
     */
    @Transactional(readOnly = true)
    public Integer getDailyTotalMinutes(LocalDate date) {
        User user = userService.getCurrentUser();
        Integer total = timeEntryRepository.getTotalMinutesForDate(user, date);
        return total != null ? total : 0;
    }

    /**
     * Check for overlapping time entries
     */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findOverlappingEntries(LocalDate date, LocalTime startTime,
                                                          LocalTime endTime, Long excludeId) {
        User user = userService.getCurrentUser();
        List<TimeEntry> overlapping = timeEntryRepository.findOverlappingEntries(
                user, date, startTime, endTime, excludeId);
        return timeEntryMapper.toResponseList(overlapping);
    }

    // Private helper methods

    private TimeEntry findTimeEntryByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        return timeEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found with id: " + id));
    }

    private Task findTaskByIdAndUser(Long taskId) {
        User user = userService.getCurrentUser();
        return taskRepository.findById(taskId)
                .filter(task -> task.getCategory().getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private TimeEntryValidationResponse validateTimeEntry(Object request, User user, Long excludeId) {
        TimeEntryValidationResponse response = new TimeEntryValidationResponse();
        response.setValid(true);
        response.setWarnings(new ArrayList<>());
        response.setErrors(new ArrayList<>());
        response.setConflicts(new ArrayList<>());

        LocalDate entryDate;
        LocalTime startTime;
        LocalTime endTime;

        // Extract fields based on request type
        if (request instanceof TimeEntryCreateRequest createReq) {
            entryDate = createReq.getEntryDate();
            startTime = createReq.getStartTime();
            endTime = createReq.getEndTime();
        } else if (request instanceof TimeEntryUpdateRequest updateReq) {
            entryDate = updateReq.getEntryDate();
            startTime = updateReq.getStartTime();
            endTime = updateReq.getEndTime();
        } else {
            throw new IllegalArgumentException("Invalid request type");
        }

        response.setDate(entryDate);

        // Validate time range
        if (!startTime.isBefore(endTime)) {
            response.getErrors().add("Start time must be before end time");
            response.setValid(false);
        }

        // Calculate duration and validate minimum
        int durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 15) {
            response.getErrors().add("Minimum time entry duration is 15 minutes");
            response.setValid(false);
        }

        // Check for overlaps
        List<TimeEntry> overlappingEntries = timeEntryRepository.findOverlappingEntries(
                user, entryDate, startTime, endTime, excludeId);

        if (!overlappingEntries.isEmpty()) {
            response.setValid(false);
            for (TimeEntry overlap : overlappingEntries) {
                TimeEntryValidationResponse.ConflictingEntry conflict =
                        new TimeEntryValidationResponse.ConflictingEntry();
                conflict.setTimeEntryId(overlap.getId());
                conflict.setTaskTitle(overlap.getTask().getTitle());
                conflict.setStartTime(overlap.getStartTime().toString());
                conflict.setEndTime(overlap.getEndTime().toString());
                conflict.setConflictType("OVERLAP");
                response.getConflicts().add(conflict);
            }
            response.getErrors().add("Time entry overlaps with existing entries");
        }

        // Business rule validations
        Integer dailyTotal = timeEntryRepository.getTotalMinutesForDate(user, entryDate);
        int currentDailyMinutes = (dailyTotal != null ? dailyTotal : 0);
        int newDailyTotal = currentDailyMinutes + durationMinutes;

        // Warning for long days
        if (newDailyTotal > 16 * 60) { // More than 16 hours
            response.getWarnings().add("Daily total exceeds 16 hours");
        }

        // Warning for very long single entry
        if (durationMinutes > 12 * 60) { // More than 12 hours
            response.getWarnings().add("Single time entry exceeds 12 hours");
        }

        // Error for impossible day
        if (newDailyTotal > 24 * 60) { // More than 24 hours
            response.getErrors().add("Daily total cannot exceed 24 hours");
            response.setValid(false);
        }

        if (!response.getValid()) {
            throw new ValidationException("Time entry validation failed: " +
                    String.join(", ", response.getErrors()));
        }

        return response;
    }
}