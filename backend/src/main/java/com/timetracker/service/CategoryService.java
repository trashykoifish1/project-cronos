package com.timetracker.service;

import com.timetracker.dto.mapper.CategoryMapper;
import com.timetracker.dto.request.CategoryCreateRequest;
import com.timetracker.dto.request.CategoryUpdateRequest;
import com.timetracker.dto.response.CategoryResponse;
import com.timetracker.entity.Category;
import com.timetracker.entity.User;
import com.timetracker.exception.ResourceNotFoundException;
import com.timetracker.exception.ValidationException;
import com.timetracker.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    /**
     * Get all categories for the current user
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        User user = userService.getCurrentUser();
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscTitleAsc(user);
        return categoryMapper.toCategoryResponseList(categories);
    }

    /**
     * Get all active (non-archived) categories for the current user
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        User user = userService.getCurrentUser();
        List<Category> categories = categoryRepository.findActiveByUser(user);
        return categoryMapper.toCategoryResponseList(categories);
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryByIdAndUser(id);
        return categoryMapper.toCategoryResponse(category);
    }

    /**
     * Create a new category
     */
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        User user = userService.getCurrentUser();

        // Validate title uniqueness
        validateTitleUniqueness(request.getTitle(), user, null);

        // Handle default category logic
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlag(user);
        }

        Category category = categoryMapper.toCategoryEntity(request);
        category.setUser(user);

        // Set sort order if not provided
        if (category.getSortOrder() == null) {
            category.setSortOrder(getNextSortOrder(user));
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category '{}' for user {}", savedCategory.getTitle(), user.getEmail());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    /**
     * Update an existing category
     */
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        User user = userService.getCurrentUser();
        Category category = findCategoryByIdAndUser(id);

        // Validate title uniqueness if title is being changed
        if (!category.getTitle().equals(request.getTitle())) {
            validateTitleUniqueness(request.getTitle(), user, id);
        }

        // Handle default category logic
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(category.getIsDefault())) {
            clearDefaultFlag(user);
        }

        categoryMapper.updateEntityFromRequest(request, category);

        Category savedCategory = categoryRepository.save(category);
        log.info("Updated category '{}' for user {}", savedCategory.getTitle(), user.getEmail());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    /**
     * Delete a category (soft delete by archiving)
     */
    public void deleteCategory(Long id) {
        User user = userService.getCurrentUser();
        Category category = findCategoryByIdAndUser(id);

        // Check if category has time entries
        boolean hasTimeEntries = timeEntryRepository.existsByTask_Category(category);
        if (hasTimeEntries) {
            // Archive instead of delete if it has time entries
            category.setIsArchived(true);
            categoryRepository.save(category);
            log.info("Archived category '{}' for user {} (has time entries)", category.getTitle(), user.getEmail());
        } else {
            // Hard delete if no time entries
            categoryRepository.delete(category);
            log.info("Deleted category '{}' for user {}", category.getTitle(), user.getEmail());
        }
    }

    /**
     * Archive/unarchive a category
     */
    public CategoryResponse archiveCategory(Long id, boolean archived) {
        Category category = findCategoryByIdAndUser(id);

        if (archived && Boolean.TRUE.equals(category.getIsDefault())) {
            throw new ValidationException("Cannot archive the default category");
        }

        category.setIsArchived(archived);
        Category savedCategory = categoryRepository.save(category);

        String action = archived ? "Archived" : "Unarchived";
        log.info("{} category '{}' for user {}", action, savedCategory.getTitle(), savedCategory.getUser().getEmail());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    /**
     * Reorder categories
     */
    public List<CategoryResponse> reorderCategories(List<Long> categoryIds) {
        User user = userService.getCurrentUser();

        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = findCategoryByIdAndUser(categoryId);
            category.setSortOrder(i);
            categoryRepository.save(category);
        }

        log.info("Reordered {} categories for user {}", categoryIds.size(), user.getEmail());
        return getActiveCategories();
    }

    /**
     * Get or create default category for user
     */
    public Category getOrCreateDefaultCategory(User user) {
        Optional<Category> defaultCategory = categoryRepository.findByUserAndIsDefault(user, true);

        if (defaultCategory.isPresent()) {
            return defaultCategory.get();
        }

        // Create default category
        Category category = new Category();
        category.setUser(user);
        category.setTitle("General");
        category.setDescription("Default category for time tracking");
        category.setIsDefault(true);
        category.setSortOrder(0);

        Category saved = categoryRepository.save(category);
        log.info("Created default category for user {}", user.getEmail());
        return saved;
    }

    // Private helper methods

    private Category findCategoryByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        return categoryRepository.findById(id)
                .filter(category -> category.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void validateTitleUniqueness(String title, User user, Long excludeId) {
        Optional<Category> existingCategory = categoryRepository.findByUserAndTitle(user, title);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(excludeId)) {
            throw new ValidationException("Category with title '" + title + "' already exists");
        }
    }

    private void clearDefaultFlag(User user) {
        Optional<Category> currentDefault = categoryRepository.findByUserAndIsDefault(user, true);
        if (currentDefault.isPresent()) {
            currentDefault.get().setIsDefault(false);
            categoryRepository.save(currentDefault.get());
        }
    }

    private Integer getNextSortOrder(User user) {
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscTitleAsc(user);
        return categories.stream()
                .mapToInt(Category::getSortOrder)
                .max()
                .orElse(0) + 1;
    }
}