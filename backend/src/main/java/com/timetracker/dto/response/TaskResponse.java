package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponse {

    private Long id;
    private Long categoryId;
    private String categoryTitle;
    private String title;
    private String description;
    private String color;
    private String icon;
    private Boolean isArchived;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics (optional, for detailed views)
    private Integer totalTimeEntries;
    private Integer totalMinutesTracked;
    private LocalDateTime lastUsed;
}