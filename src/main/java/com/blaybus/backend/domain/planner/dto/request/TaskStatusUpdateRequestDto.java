package com.blaybus.backend.domain.planner.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaskStatusUpdateRequestDto {

    @NotNull(message = "isCompleted는 필수입니다.")
    private Boolean isCompleted;
}
