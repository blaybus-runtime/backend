package com.blaybus.backend.domain.content.dto.response;

import com.blaybus.backend.domain.content.StudyColumn;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudyColumnSummaryResponse {

    private Long columnId;
    private String title;
    private String mentorName;
    private LocalDateTime createdAt;

    @Builder
    public StudyColumnSummaryResponse(Long columnId, String title, String mentorName, LocalDateTime createdAt) {
        this.columnId = columnId;
        this.title = title;
        this.mentorName = mentorName;
        this.createdAt = createdAt;
    }

    public static StudyColumnSummaryResponse from(StudyColumn entity) {
        return StudyColumnSummaryResponse.builder()
                .columnId(entity.getId())
                .title(entity.getTitle())
                .mentorName(entity.getMentor().getUser().getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
