package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByTask_Id(Long taskId);

    Optional<Feedback> findByTask_Id(Long taskId);
}
