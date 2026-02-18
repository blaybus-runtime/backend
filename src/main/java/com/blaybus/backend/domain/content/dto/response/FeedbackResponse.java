package com.blaybus.backend.domain.content.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackResponse {

    public record MentorInfo(
            Long mentorId,
            String mentorName,
            String profileImage
    ) {}

    public record FileInfo(
            Long fileId,
            String fileName,
            String fileUrl
    ) {}

    public record Create(
            Long feedbackId,
            Long assignmentId,
            MentorInfo mentor,
            String content,
            LocalDateTime createdAt,
            List<FileInfo> files
    ) {}
}
