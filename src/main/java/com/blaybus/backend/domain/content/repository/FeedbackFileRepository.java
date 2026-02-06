package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.FeedbackFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackFileRepository extends JpaRepository<FeedbackFile, Long> {
    List<FeedbackFile> findAllByFeedback_Id(Long feedbackId);
}


