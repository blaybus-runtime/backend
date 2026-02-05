package com.blaybus.backend.domain.content.dto.response;

import java.time.LocalDateTime;

public class FeedbackResponse {

    public record MentorInfo(
            Long mentorId,
            String mentorName,
            String profileImage
    ) {}

    public record Create(
            Long feedbackId,
            Long assignmentId,
            MentorInfo mentor,
            String content,
            LocalDateTime createdAt
    ) {}
}
