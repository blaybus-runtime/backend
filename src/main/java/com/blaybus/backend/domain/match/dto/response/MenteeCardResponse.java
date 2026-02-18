package com.blaybus.backend.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenteeCardResponse {
    private Long menteeId;
    private String name;
    private String profileImageUrl;
    private Integer unwrittenFeedbackCount;
    private String highSchool;
    private Integer grade;
    private List<String> subjects;
    private String targetUniv;
}
