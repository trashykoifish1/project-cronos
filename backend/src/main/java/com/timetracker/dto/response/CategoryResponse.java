package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponse {

    private Long id;
    private String title;
    private String description;
    private Boolean isArchived;
    private Boolean isDefault;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: Include tasks if needed
    private List<TaskResponse> tasks;

    // Statistics
    private Integer taskCount;
    private Integer activeTaskCount;
}