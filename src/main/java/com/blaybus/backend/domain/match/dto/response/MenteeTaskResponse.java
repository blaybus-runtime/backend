package com.blaybus.backend.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenteeTaskResponse {
    private Long menteeId;
    private String menteeName;
    private Long plannerId;
    private Long taskId;
    private String taskContent;
    private boolean isCompleted;
}
