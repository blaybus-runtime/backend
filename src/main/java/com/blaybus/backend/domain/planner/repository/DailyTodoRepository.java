package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.StudyPlanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTodoRepository extends JpaRepository<StudyPlanner, Long> {

    Optional<StudyPlanner> findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(Long menteeUserId, LocalDate planDate);
}
