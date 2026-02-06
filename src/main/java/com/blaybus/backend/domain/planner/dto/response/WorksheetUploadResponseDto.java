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
    private String materialType;
    private String fileUrl;

    // ✅ 공통화
    private Long uploaderId;
    private String uploaderRole; // "MENTOR" | "MENTEE"

    private LocalDateTime createdAt;
}
