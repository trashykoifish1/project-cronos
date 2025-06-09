package com.timetracker.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BulkTimeEntryRequest {

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotEmpty(message = "At least one time entry is required")
    @Valid
    private List<TimeEntryCreateRequest> timeEntries;

    // Flag to indicate if we should replace all entries for the date
    private Boolean replaceExisting = false;
}