package com.timetracker.repository;

import com.timetracker.entity.Category;
import com.timetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserAndIsArchivedOrderBySortOrderAscTitleAsc(User user, Boolean isArchived);

    List<Category> findByUserOrderBySortOrderAscTitleAsc(User user);

    Optional<Category> findByUserAndTitle(User user, String title);

    Optional<Category> findByUserAndIsDefault(User user, Boolean isDefault);

    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.isArchived = false ORDER BY c.sortOrder ASC, c.title ASC")
    List<Category> findActiveByUser(@Param("user") User user);

    boolean existsByUserAndTitle(User user, String title);
}