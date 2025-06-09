package com.timetracker.controller;

import com.timetracker.dto.request.TaskCreateRequest;
import com.timetracker.dto.request.TaskUpdateRequest;
import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.TaskResponse;
import com.timetracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management operations")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get tasks by category", description = "Retrieve all tasks for a specific category")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        List<TaskResponse> tasks = taskService.getTasksByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/category/{categoryId}/active")
    @Operation(summary = "Get active tasks by category", description = "Retrieve all non-archived tasks for a specific category")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getActiveTasksByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        List<TaskResponse> tasks = taskService.getActiveTasksByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active tasks", description = "Retrieve all active tasks for the current user across all categories")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllActiveTasksForUser() {
        List<TaskResponse> tasks = taskService.getAllActiveTasksForUser();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping
    @Operation(summary = "Create new task", description = "Create a new task in a category")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update an existing task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete a task (archives if it has time entries)")
    public ResponseEntity<ApiResponse<String>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive/unarchive task", description = "Toggle archive status of a task")
    public ResponseEntity<ApiResponse<TaskResponse>> archiveTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "Archive status") @RequestParam boolean archived) {
        TaskResponse task = taskService.archiveTask(id, archived);
        String message = archived ? "Task archived successfully" : "Task unarchived successfully";
        return ResponseEntity.ok(ApiResponse.success(message, task));
    }

    @PutMapping("/category/{categoryId}/reorder")
    @Operation(summary = "Reorder tasks in category", description = "Update the sort order of tasks within a category")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> reorderTasks(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Ordered list of task IDs") @RequestBody List<Long> taskIds) {
        List<TaskResponse> tasks = taskService.reorderTasks(categoryId, taskIds);
        return ResponseEntity.ok(ApiResponse.success("Tasks reordered successfully", tasks));
    }

    @PutMapping("/{taskId}/move-to-category/{newCategoryId}")
    @Operation(summary = "Move task to different category", description = "Move a task from one category to another")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTaskToCategory(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "New category ID") @PathVariable Long newCategoryId) {
        TaskResponse task = taskService.moveTaskToCategory(taskId, newCategoryId);
        return ResponseEntity.ok(ApiResponse.success("Task moved successfully", task));
    }
}