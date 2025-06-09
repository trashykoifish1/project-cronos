package com.timetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

@Entity
@Table(name = "time_entries",
        indexes = {
                @Index(name = "idx_time_entries_user_date", columnList = "user_id, entry_date"),
                @Index(name = "idx_time_entries_task", columnList = "task_id"),
                @Index(name = "idx_time_entries_date_time", columnList = "entry_date, start_time, end_time")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class TimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Positive
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_billable")
    private Boolean isBillable = false;

    // Calculated field - not stored in database
    @Transient
    public Duration getDuration() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return Duration.ZERO;
    }

    // Helper method to calculate duration in minutes
    public void calculateDurationMinutes() {
        if (startTime != null && endTime != null) {
            Duration duration = getDuration();
            this.durationMinutes = (int) duration.toMinutes();
        }
    }

    // Validation method
    @PrePersist
    @PreUpdate
    private void validateTimeEntry() {
        if (startTime != null && endTime != null) {
            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            calculateDurationMinutes();
        }
    }

    // Helper methods for business logic
    public boolean overlapsWith(TimeEntry other) {
        if (other == null || !this.entryDate.equals(other.entryDate)) {
            return false;
        }

        return !(this.endTime.isBefore(other.startTime) ||
                this.startTime.isAfter(other.endTime) ||
                this.endTime.equals(other.startTime) ||
                this.startTime.equals(other.endTime));
    }

    public boolean isValidDuration() {
        return durationMinutes != null && durationMinutes >= 15; // Minimum 15 minutes
    }
}