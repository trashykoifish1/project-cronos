package com.timetracker.repository;

import com.timetracker.entity.Category;
import com.timetracker.entity.Task;
import com.timetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCategoryAndIsArchivedOrderBySortOrderAscTitleAsc(Category category, Boolean isArchived);

    List<Task> findByCategoryOrderBySortOrderAscTitleAsc(Category category);

    Optional<Task> findByCategoryAndTitle(Category category, String title);

    Optional<Task> findByCategoryAndColor(Category category, String color);

    @Query("SELECT t FROM Task t WHERE t.category = :category AND t.isArchived = false ORDER BY t.sortOrder ASC, t.title ASC")
    List<Task> findActiveBycategory(@Param("category") Category category);

    @Query("SELECT t FROM Task t JOIN t.category c WHERE c.user = :user AND t.isArchived = false ORDER BY c.title ASC, t.sortOrder ASC, t.title ASC")
    List<Task> findActiveByUser(@Param("user") User user);

    @Query("SELECT t FROM Task t JOIN t.category c WHERE c.user = :user ORDER BY c.title ASC, t.sortOrder ASC, t.title ASC")
    List<Task> findAllByUser(@Param("user") User user);

    boolean existsByCategoryAndTitle(Category category, String title);

    boolean existsByCategoryAndColor(Category category, String color);
}