package com.timetracker.service;

import com.timetracker.dto.mapper.TaskMapper;
import com.timetracker.dto.request.TaskCreateRequest;
import com.timetracker.dto.request.TaskUpdateRequest;
import com.timetracker.dto.response.TaskResponse;
import com.timetracker.entity.Category;
import com.timetracker.entity.Task;
import com.timetracker.entity.User;
import com.timetracker.exception.ResourceNotFoundException;
import com.timetracker.exception.ValidationException;
import com.timetracker.repository.CategoryRepository;
import com.timetracker.repository.TaskRepository;
import com.timetracker.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    /**
     * Get all tasks for a specific category
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCategory(Long categoryId) {
        Category category = findCategoryByIdAndUser(categoryId);
        List<Task> tasks = taskRepository.findByCategoryOrderBySortOrderAscTitleAsc(category);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get all active (non-archived) tasks for a specific category
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getActiveTasksByCategory(Long categoryId) {
        Category category = findCategoryByIdAndUser(categoryId);
        List<Task> tasks = taskRepository.findActiveBycategory(category);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get all tasks for the current user (across all categories)
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForUser() {
        User user = userService.getCurrentUser();
        List<Task> tasks = taskRepository.findAllByUser(user);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get all active tasks for the current user (across all categories)
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllActiveTasksForUser() {
        User user = userService.getCurrentUser();
        List<Task> tasks = taskRepository.findActiveByUser(user);
        return taskMapper.toTaskResponseList(tasks);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskByIdAndUser(id);
        return taskMapper.toTaskResponse(task);
    }

    /**
     * Create a new task
     */
    public TaskResponse createTask(TaskCreateRequest request) {
        User user = userService.getCurrentUser();
        Category category = findCategoryByIdAndUser(request.getCategoryId());

        // Validate title uniqueness within category
        validateTitleUniquenessInCategory(request.getTitle(), category, null);

        // Validate color uniqueness within category
        validateColorUniquenessInCategory(request.getColor(), category, null);

        Task task = taskMapper.toEntity(request);
        task.setCategory(category);

        // Set sort order if not provided
        if (task.getSortOrder() == null) {
            task.setSortOrder(getNextSortOrderInCategory(category));
        }

        Task savedTask = taskRepository.save(task);
        log.info("Created task '{}' in category '{}' for user {}",
                savedTask.getTitle(), category.getTitle(), user.getEmail());

        return taskMapper.toTaskResponse(savedTask);
    }

    /**
     * Update an existing task
     */
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        User user = userService.getCurrentUser();
        Task task = findTaskByIdAndUser(id);

        // Validate title uniqueness if title is being changed
        if (!task.getTitle().equals(request.getTitle())) {
            validateTitleUniquenessInCategory(request.getTitle(), task.getCategory(), id);
        }

        // Validate color uniqueness if color is being changed
        if (!task.getColor().equals(request.getColor())) {
            validateColorUniquenessInCategory(request.getColor(), task.getCategory(), id);
        }

        taskMapper.updateEntityFromRequest(request, task);

        Task savedTask = taskRepository.save(task);
        log.info("Updated task '{}' for user {}", savedTask.getTitle(), user.getEmail());

        return taskMapper.toTaskResponse(savedTask);
    }

    /**
     * Delete a task (soft delete by archiving if it has time entries)
     */
    public void deleteTask(Long id) {
        User user = userService.getCurrentUser();
        Task task = findTaskByIdAndUser(id);

        // Check if task has time entries
        boolean hasTimeEntries = timeEntryRepository.existsByTask(task);
        if (hasTimeEntries) {
            // Archive instead of delete if it has time entries
            task.setIsArchived(true);
            taskRepository.save(task);
            log.info("Archived task '{}' for user {} (has time entries)", task.getTitle(), user.getEmail());
        } else {
            // Hard delete if no time entries
            taskRepository.delete(task);
            log.info("Deleted task '{}' for user {}", task.getTitle(), user.getEmail());
        }
    }

    /**
     * Archive/unarchive a task
     */
    public TaskResponse archiveTask(Long id, boolean archived) {
        Task task = findTaskByIdAndUser(id);

        task.setIsArchived(archived);
        Task savedTask = taskRepository.save(task);

        String action = archived ? "Archived" : "Unarchived";
        log.info("{} task '{}' for user {}", action, savedTask.getTitle(),
                savedTask.getCategory().getUser().getEmail());

        return taskMapper.toTaskResponse(savedTask);
    }

    /**
     * Reorder tasks within a category
     */
    public List<TaskResponse> reorderTasks(Long categoryId, List<Long> taskIds) {
        Category category = findCategoryByIdAndUser(categoryId);

        for (int i = 0; i < taskIds.size(); i++) {
            Long taskId = taskIds.get(i);
            Task task = findTaskByIdAndUser(taskId);

            // Ensure task belongs to the specified category
            if (!task.getCategory().getId().equals(categoryId)) {
                throw new ValidationException("Task " + taskId + " does not belong to category " + categoryId);
            }

            task.setSortOrder(i);
            taskRepository.save(task);
        }

        log.info("Reordered {} tasks in category '{}' for user {}",
                taskIds.size(), category.getTitle(), category.getUser().getEmail());

        return getActiveTasksByCategory(categoryId);
    }

    /**
     * Move task to different category
     */
    public TaskResponse moveTaskToCategory(Long taskId, Long newCategoryId) {
        User user = userService.getCurrentUser();
        Task task = findTaskByIdAndUser(taskId);
        Category newCategory = findCategoryByIdAndUser(newCategoryId);

        // Validate title uniqueness in new category
        validateTitleUniquenessInCategory(task.getTitle(), newCategory, taskId);

        // Validate color uniqueness in new category
        validateColorUniquenessInCategory(task.getColor(), newCategory, taskId);

        task.setCategory(newCategory);
        task.setSortOrder(getNextSortOrderInCategory(newCategory));

        Task savedTask = taskRepository.save(task);
        log.info("Moved task '{}' to category '{}' for user {}",
                savedTask.getTitle(), newCategory.getTitle(), user.getEmail());

        return taskMapper.toTaskResponse(savedTask);
    }

    /**
     * Get default task for a category (creates one if none exists)
     */
    public Task getOrCreateDefaultTask(Category category) {
        List<Task> tasks = taskRepository.findActiveBycategory(category);

        if (!tasks.isEmpty()) {
            return tasks.get(0); // Return first active task
        }

        // Create default task
        Task task = new Task();
        task.setCategory(category);
        task.setTitle("General Task");
        task.setColor("#3498db"); // Blue
        task.setIcon("clock");
        task.setSortOrder(0);

        Task saved = taskRepository.save(task);
        log.info("Created default task for category '{}' for user {}",
                category.getTitle(), category.getUser().getEmail());
        return saved;
    }

    // Private helper methods

    private Task findTaskByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        return taskRepository.findById(id)
                .filter(task -> task.getCategory().getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private Category findCategoryByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        return categoryRepository.findById(id)
                .filter(category -> category.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void validateTitleUniquenessInCategory(String title, Category category, Long excludeId) {
        Optional<Task> existingTask = taskRepository.findByCategoryAndTitle(category, title);
        if (existingTask.isPresent() && !existingTask.get().getId().equals(excludeId)) {
            throw new ValidationException("Task with title '" + title + "' already exists in this category");
        }
    }

    private void validateColorUniquenessInCategory(String color, Category category, Long excludeId) {
        Optional<Task> existingTask = taskRepository.findByCategoryAndColor(category, color);
        if (existingTask.isPresent() && !existingTask.get().getId().equals(excludeId)) {
            throw new ValidationException("Task with color '" + color + "' already exists in this category");
        }
    }

    private Integer getNextSortOrderInCategory(Category category) {
        List<Task> tasks = taskRepository.findByCategoryOrderBySortOrderAscTitleAsc(category);
        return tasks.stream()
                .mapToInt(Task::getSortOrder)
                .max()
                .orElse(0) + 1;
    }
}