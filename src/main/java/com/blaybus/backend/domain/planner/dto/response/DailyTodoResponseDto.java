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
    private ProgressDto progress;
    private List<TodoDto> todos;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProgressDto {
        private Integer completionRate; // 0~100
        private List<SubjectProgressDto> bySubject;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubjectProgressDto {
        private String subject;  // "국어", "영어", "수학" 등 (DB VARCHAR)
        private Integer percent; // 0~100
    }

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
