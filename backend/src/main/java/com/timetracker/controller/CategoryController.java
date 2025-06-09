package com.timetracker.controller;

import com.timetracker.dto.request.CategoryCreateRequest;
import com.timetracker.dto.request.CategoryUpdateRequest;
import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.CategoryResponse;
import com.timetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories for the current user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieve all non-archived categories for the current user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        List<CategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    @Operation(summary = "Create new category", description = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category (archives if it has time entries)")
    public ResponseEntity<ApiResponse<String>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive/unarchive category", description = "Toggle archive status of a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> archiveCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "Archive status") @RequestParam boolean archived) {
        CategoryResponse category = categoryService.archiveCategory(id, archived);
        String message = archived ? "Category archived successfully" : "Category unarchived successfully";
        return ResponseEntity.ok(ApiResponse.success(message, category));
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder categories", description = "Update the sort order of categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> reorderCategories(
            @Parameter(description = "Ordered list of category IDs") @RequestBody List<Long> categoryIds) {
        List<CategoryResponse> categories = categoryService.reorderCategories(categoryIds);
        return ResponseEntity.ok(ApiResponse.success("Categories reordered successfully", categories));
    }
}