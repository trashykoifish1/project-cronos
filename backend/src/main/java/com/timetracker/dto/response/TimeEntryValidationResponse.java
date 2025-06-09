package com.timetracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TimeEntryValidationResponse {
    private LocalDate date;
    private Boolean valid;
    private List<String> warnings;
    private List<String> errors;
    private List<ConflictingEntry> conflicts;

    @Data
    public static class ConflictingEntry {
        private Long timeEntryId;
        private String taskTitle;
        private String startTime;
        private String endTime;
        private String conflictType; // "OVERLAP", "ADJACENT", "DUPLICATE"
    }
}