package com.blaybus.backend.domain.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MentorTodoBatchResponse {

    private Long menteeId;
    private Integer createdCount;
    private List<MentorTodoResponse> tasks;
}
