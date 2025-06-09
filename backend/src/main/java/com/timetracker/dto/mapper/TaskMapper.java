package com.timetracker.dto.mapper;

import com.timetracker.dto.request.TaskCreateRequest;
import com.timetracker.dto.request.TaskUpdateRequest;
import com.timetracker.dto.response.TaskResponse;
import com.timetracker.entity.Category;
import com.timetracker.entity.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    // Entity to Response (with full statistics)
    @Named("toTaskResponse")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryTitle", source = "category.title")
    @Mapping(target = "totalTimeEntries", expression = "java(task.getTimeEntries().size())")
    @Mapping(target = "totalMinutesTracked", expression = "java(task.getTimeEntries().stream().mapToInt(te -> te.getDurationMinutes()).sum())")
    @Mapping(target = "lastUsed", expression = "java(task.getTimeEntries().stream().map(te -> te.getCreatedAt()).max(java.time.LocalDateTime::compareTo).orElse(null))")
    TaskResponse toTaskResponse(Task task);

    // Entity to Response without statistics (for performance)
    @Named("toTaskResponseWithoutStats")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryTitle", source = "category.title")
    @Mapping(target = "totalTimeEntries", ignore = true)
    @Mapping(target = "totalMinutesTracked", ignore = true)
    @Mapping(target = "lastUsed", ignore = true)
    TaskResponse toTaskResponseWithoutStats(Task task);

    // List mapping - specify which method to use (without stats for performance)
    @IterableMapping(qualifiedByName = "toTaskResponseWithoutStats")
    List<TaskResponse> toTaskResponseList(List<Task> tasks);

    // Request to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskCreateRequest request);

    // Update entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(TaskUpdateRequest request, @MappingTarget Task task);

    // Helper method to set category
    @AfterMapping
    default void setCategory(@MappingTarget Task task, Category category) {
        if (category != null) {
            task.setCategory(category);
        }
    }
}