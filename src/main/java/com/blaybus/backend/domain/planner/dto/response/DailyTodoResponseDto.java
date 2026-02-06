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
        private String title;
        private String goal;
        private Boolean isFeedbackDone;

        // ✅ 추가: 과제에 연결된 파일 목록
        private List<WorksheetDto> worksheets;
    }

    // ✅ 추가: 파일 DTO
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorksheetDto {
        private Long worksheetId;
        private String title;
        private String subject;
        private String fileUrl;
        private String weekdays; // task_worksheet.weekdays 그대로 내려줌
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
