package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByTask_Id(Long taskId);
}
