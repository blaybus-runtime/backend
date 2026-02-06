package com.blaybus.backend.domain.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubmissionFileResponseDto {
    private Long fileId;
    private String fileName;
    private String fileUrl;
}
