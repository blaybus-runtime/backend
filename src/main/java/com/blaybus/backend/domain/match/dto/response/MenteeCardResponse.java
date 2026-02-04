package com.blaybus.backend.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenteeCardResponse {
    private Long menteeId;
    private String name;
    private String profileImageUrl;
    private String school;
    private String grade;
    private boolean isDailyTodoCompleted;
}
