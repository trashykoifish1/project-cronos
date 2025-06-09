package com.timetracker.repository;

import com.timetracker.entity.Category;
import com.timetracker.entity.Task;
import com.timetracker.entity.TimeEntry;
import com.timetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    List<TimeEntry> findByUserAndEntryDateOrderByStartTimeAsc(User user, LocalDate entryDate);

    List<TimeEntry> findByUserAndEntryDateBetweenOrderByEntryDateAscStartTimeAsc(
            User user, LocalDate startDate, LocalDate endDate);

    List<TimeEntry> findByTaskOrderByEntryDateDescStartTimeDesc(Task task);

    @Query("SELECT te FROM TimeEntry te WHERE te.user = :user AND te.entryDate = :date " +
            "AND ((te.startTime >= :startTime AND te.startTime < :endTime) OR " +
            "(te.endTime > :startTime AND te.endTime <= :endTime) OR " +
            "(te.startTime <= :startTime AND te.endTime >= :endTime)) " +
            "AND (:excludeId IS NULL OR te.id != :excludeId)")
    List<TimeEntry> findOverlappingEntries(@Param("user") User user,
                                           @Param("date") LocalDate date,
                                           @Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime,
                                           @Param("excludeId") Long excludeId);

    @Query("SELECT SUM(te.durationMinutes) FROM TimeEntry te WHERE te.user = :user AND te.entryDate = :date")
    Integer getTotalMinutesForDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT SUM(te.durationMinutes) FROM TimeEntry te WHERE te.task = :task " +
            "AND te.entryDate BETWEEN :startDate AND :endDate")
    Integer getTotalMinutesForTaskInPeriod(@Param("task") Task task,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    boolean existsByTask(Task task);

    // Check if a category has any time entries (through its tasks)
    @Query("SELECT COUNT(te) > 0 FROM TimeEntry te WHERE te.task.category = :category")
    boolean existsByTask_Category(@Param("category") Category category);

    // Get time entries by category (through tasks)
    @Query("SELECT te FROM TimeEntry te WHERE te.task.category = :category ORDER BY te.entryDate DESC, te.startTime DESC")
    List<TimeEntry> findByTask_Category(@Param("category") Category category);
}