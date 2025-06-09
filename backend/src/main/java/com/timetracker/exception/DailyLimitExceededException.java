package com.timetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DailyLimitExceededException extends RuntimeException {

    private final int maxHours;
    private final int actualHours;

    public DailyLimitExceededException(int maxHours) {
        super("Daily time limit of " + maxHours + " hours would be exceeded");
        this.maxHours = maxHours;
        this.actualHours = 0;
    }

    public DailyLimitExceededException(int maxHours, int actualHours) {
        super("Daily time limit of " + maxHours + " hours exceeded (actual: " + actualHours + " hours)");
        this.maxHours = maxHours;
        this.actualHours = actualHours;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public int getActualHours() {
        return actualHours;
    }
}