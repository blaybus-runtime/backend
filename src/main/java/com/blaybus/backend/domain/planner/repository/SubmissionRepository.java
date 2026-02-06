package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByTask_IdAndMenteeId(Long taskId, Long menteeId);
}
