package com.blaybus.backend.domain.planner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskStatusUpdateResponseDto {

    private Long taskId;
    private Boolean isCompleted;
}
