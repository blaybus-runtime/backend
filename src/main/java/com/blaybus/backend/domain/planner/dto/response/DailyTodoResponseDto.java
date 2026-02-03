package com.blaybus.backend.domain.planner.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTodoResponseDto {

    private Long menteeId;
    private LocalDate date;
    private List<TodoDto> todos;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor


    public static class TodoDto {
        private Long id;
        private String content;
        private String subject;
        private Boolean isCompleted;
        private Integer priority;
        private String taskType; // SELF / ASSIGNMENT
    }
}
