package com.blaybus.backend.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MenteeFeedbackResponse {
    private Long menteeId;
    private String menteeName;

    private Long plannerId;
    private Long taskId;

    private String subject;
    private String taskContent;
    private LocalDateTime completedAt;
}
