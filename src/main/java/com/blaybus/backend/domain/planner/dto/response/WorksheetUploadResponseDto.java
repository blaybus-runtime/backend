package com.blaybus.backend.domain.planner.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorksheetUploadResponseDto {

    private Long worksheetId;
    private String title;
    private String subject;
    private String materialType;   // "FILE" or "BOOK"
    private String fileUrl;
    private Long mentorId;
    private LocalDateTime createdAt;
}
