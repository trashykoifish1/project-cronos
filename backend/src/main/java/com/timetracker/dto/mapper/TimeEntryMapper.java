package com.timetracker.dto.mapper;

import com.timetracker.dto.request.TimeEntryCreateRequest;
import com.timetracker.dto.request.TimeEntryUpdateRequest;
import com.timetracker.dto.response.TimeEntryResponse;
import com.timetracker.entity.Task;
import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {

    // Entity to Response
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "taskColor", source = "task.color")
    @Mapping(target = "taskIcon", source = "task.icon")
    @Mapping(target = "categoryId", source = "task.category.id")
    @Mapping(target = "categoryTitle", source = "task.category.title")
    TimeEntryResponse toTimeEntryResponse(TimeEntry timeEntry);

    List<TimeEntryResponse> toResponseList(List<TimeEntry> timeEntries);

    // Request to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true) // Will be calculated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TimeEntry toEntity(TimeEntryCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true) // Will be calculated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TimeEntry toEntity(TimeEntryUpdateRequest request);

    // Update entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true) // Will be calculated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(TimeEntryUpdateRequest request, @MappingTarget TimeEntry timeEntry);

    // Helper methods to set relationships and calculate duration
    @AfterMapping
    default void setRelationshipsAndCalculateDuration(@MappingTarget TimeEntry timeEntry, User user, Task task) {
        if (user != null) {
            timeEntry.setUser(user);
        }
        if (task != null) {
            timeEntry.setTask(task);
        }
        // Calculate duration if start and end times are set
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            timeEntry.calculateDurationMinutes();
        }
    }

    // Utility method for formatting time duration
    default String formatDuration(Integer minutes) {
        if (minutes == null || minutes == 0) {
            return "0m";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "m";
        } else if (remainingMinutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + remainingMinutes + "m";
        }
    }
}