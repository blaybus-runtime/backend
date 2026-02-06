package com.blaybus.backend.domain.planner.dto.response;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.planner.Submission;
import com.blaybus.backend.domain.planner.TodoTask;
// TaskWorksheet import가 필요합니다. (패키지 경로 확인 필요)
import com.blaybus.backend.domain.planner.TaskWorksheet;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TaskDetailResponse(
        Long taskId,
        String content,         // 할 일 내용
        String subject,         // 과목
        String feedbackContent, // 피드백 내용

        List<WorksheetDto> worksheets,  // 학습지 목록
        List<SubmissionDto> submissions // 제출물 목록
) {
    public static TaskDetailResponse from(TodoTask task) {
        // 1. 학습지 목록 추출 로직
        List<WorksheetDto> worksheetDtos = new ArrayList<>();

        // (A) 새로운 구조: TaskWorksheet 리스트에서 진짜 Worksheet 꺼내기
        if (task.getTaskWorksheets() != null) {
            List<WorksheetDto> list = task.getTaskWorksheets().stream()
                    .map(TaskWorksheet::getWorksheet) // 중간 테이블에서 Worksheet 객체 추출
                    .filter(Objects::nonNull)         // 혹시 모를 null 방지
                    .map(WorksheetDto::from)          // DTO 변환
                    .toList();
            worksheetDtos.addAll(list);
        }

        // (B) 레거시 호환: 예전 worksheet 필드에 값이 있다면 그것도 포함 (팀원이 제거 전까지 안전장치)
        if (task.getWorksheet() != null) {
            // 중복 방지를 위해 ID 체크 후 추가해도 되지만, 일단 단순 추가
            boolean alreadyExists = worksheetDtos.stream()
                    .anyMatch(w -> w.worksheetId().equals(task.getWorksheet().getId()));

            if (!alreadyExists) {
                worksheetDtos.add(WorksheetDto.from(task.getWorksheet()));
            }
        }

        return TaskDetailResponse.builder()
                .taskId(task.getId())
                .content(task.getContent()) // DB의 content 컬럼
                .subject(task.getSubject())
                // 피드백이 존재하면 내용 반환
                .feedbackContent(task.getFeedback() != null ? task.getFeedback().getContent() : null)
                .worksheets(worksheetDtos)
                .submissions(task.getSubmissions().stream()
                        .map(SubmissionDto::from)
                        .toList())
                .build();
    }

    // [내부 DTO] 학습지 정보
    public record WorksheetDto(
            Long worksheetId,
            String title,
            String fileUrl
    ) {
        public static WorksheetDto from(Worksheet worksheet) {
            return new WorksheetDto(
                    worksheet.getId(),
                    worksheet.getTitle(),
                    worksheet.getFileUrl()
            );
        }
    }

    // [내부 DTO] 제출물 정보
    public record SubmissionDto(
            Long submissionId,
            String fileName,
            String fileUrl,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
            LocalDateTime createdAt
    ) {
        public static SubmissionDto from(Submission submission) {
            return new SubmissionDto(
                    submission.getId(),
                    submission.getFileName(),
                    submission.getFileUrl(),
                    submission.getCreatedAt()
            );
        }
    }
}