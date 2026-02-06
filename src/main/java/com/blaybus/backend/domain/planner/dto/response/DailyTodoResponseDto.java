package com.blaybus.backend.domain.planner.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTodoResponseDto {

    private Long menteeId;
    private Long plannerId;
    private LocalDate date;
    private List<TodoDto> todos;
    private List<TimeRecordDto> timeRecords;

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

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeRecordDto {
        private Long id;
        private String subject;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
