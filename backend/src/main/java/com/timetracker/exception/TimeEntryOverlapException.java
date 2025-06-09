package com.timetracker.exception;

import com.timetracker.entity.TimeEntry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(HttpStatus.CONFLICT)
public class TimeEntryOverlapException extends RuntimeException {

    private final List<TimeEntry> conflictingEntries;

    public TimeEntryOverlapException(List<TimeEntry> conflictingEntries) {
        super("Time entry overlaps with existing entries");
        this.conflictingEntries = conflictingEntries;
    }

    public TimeEntryOverlapException(String message, List<TimeEntry> conflictingEntries) {
        super(message);
        this.conflictingEntries = conflictingEntries;
    }

    public List<TimeEntry> getConflictingEntries() {
        return conflictingEntries;
    }
}