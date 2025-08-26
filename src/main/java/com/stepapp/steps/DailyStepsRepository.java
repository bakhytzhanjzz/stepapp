package com.stepapp.steps;

import com.stepapp.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStepsRepository extends JpaRepository<DailySteps, Long> {
    Optional<DailySteps> findByUserAndDate(User user, LocalDate date);
    List<DailySteps> findAllByUserAndDateBetween(User user, LocalDate from, LocalDate to);

    // For leaderboards: fetch top entries for a set of users
    @Query("SELECT d FROM DailySteps d WHERE d.date = :date AND d.user IN :users ORDER BY d.stepsTotal DESC")
    List<DailySteps> findTopByDateAndUsers(LocalDate date, List<User> users, Pageable pageable);

    @Query("SELECT d FROM DailySteps d WHERE d.date = :date ORDER BY d.stepsTotal DESC")
    List<DailySteps> findTopByDate(LocalDate date, Pageable pageable);
}
