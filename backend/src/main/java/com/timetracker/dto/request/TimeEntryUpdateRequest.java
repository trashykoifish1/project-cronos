package com.timetracker.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeEntryUpdateRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Boolean isBillable;
}