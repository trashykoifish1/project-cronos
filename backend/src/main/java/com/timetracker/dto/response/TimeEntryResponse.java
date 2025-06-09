package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class TimeEntryResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private String taskColor;
    private String taskIcon;
    private Long categoryId;
    private String categoryTitle;
    private LocalDate entryDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String description;
    private Boolean isBillable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}