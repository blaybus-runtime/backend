package com.blaybus.backend.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenteeCardResponse {
    private Long menteeId;
    private String name;
    private String profileImageUrl;
    private Integer unwrittenFeedbackCount;
}
