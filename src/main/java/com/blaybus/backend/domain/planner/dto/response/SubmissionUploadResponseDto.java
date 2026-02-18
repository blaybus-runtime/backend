package com.blaybus.backend.domain.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SubmissionUploadResponseDto {
    private Long assignmentId; // submission id
    private Long menteeId;
    private Long taskId;

    private String status;
    private LocalDateTime submittedAt;

    private List<SubmissionFileResponseDto> files;
}
