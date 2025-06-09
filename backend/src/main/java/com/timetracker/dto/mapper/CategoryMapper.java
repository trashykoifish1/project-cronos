package com.timetracker.dto.mapper;

import com.timetracker.dto.request.CategoryCreateRequest;
import com.timetracker.dto.request.CategoryUpdateRequest;
import com.timetracker.dto.response.CategoryResponse;
import com.timetracker.entity.Category;
import com.timetracker.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TaskMapper.class})
public interface CategoryMapper {

    // Entity to Response (main method)
    @Named("toCategoryResponse")
    @Mapping(target = "taskCount", expression = "java(category.getTasks().size())")
    @Mapping(target = "activeTaskCount", expression = "java((int) category.getTasks().stream().filter(task -> !task.getIsArchived()).count())")
    CategoryResponse toCategoryResponse(Category category);

    // Entity to Response without tasks (to avoid circular reference)
    @Named("toCategoryResponseWithoutTasks")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "taskCount", expression = "java(category.getTasks().size())")
    @Mapping(target = "activeTaskCount", expression = "java((int) category.getTasks().stream().filter(task -> !task.getIsArchived()).count())")
    CategoryResponse toCategoryResponseWithoutTasks(Category category);

    // List mapping - specify which method to use
    @IterableMapping(qualifiedByName = "toCategoryResponseWithoutTasks")
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    // Request to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toCategoryEntity(CategoryCreateRequest request);

    // Update entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);

    // Helper method to set user
    @AfterMapping
    default void setUser(@MappingTarget Category category, User user) {
        if (user != null) {
            category.setUser(user);
        }
    }
}