package com.timetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"category_id", "title"}),
                @UniqueConstraint(columnNames = {"category_id", "color"})
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String title;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    @Column(nullable = false, length = 7)
    private String color;

    @Size(max = 50)
    @Column(length = 50)
    private String icon;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Relationships
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    // Helper methods
    public void addTimeEntry(TimeEntry timeEntry) {
        timeEntries.add(timeEntry);
        timeEntry.setTask(this);
    }

    public void removeTimeEntry(TimeEntry timeEntry) {
        timeEntries.remove(timeEntry);
        timeEntry.setTask(null);
    }

    // Derived properties
    public User getUser() {
        return category != null ? category.getUser() : null;
    }
}