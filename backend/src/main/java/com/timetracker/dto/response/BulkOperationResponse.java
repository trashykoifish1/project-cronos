package com.timetracker.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BulkOperationResponse {
    private Integer successCount;
    private Integer failureCount;
    private Integer totalProcessed;
    private List<String> errors;
    private List<String> warnings;
    private List<Long> createdIds;
    private List<Long> updatedIds;
    private List<Long> skippedIds;
}