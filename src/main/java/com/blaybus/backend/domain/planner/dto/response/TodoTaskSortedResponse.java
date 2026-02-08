package com.blaybus.backend.domain.planner.dto.response;

import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.global.enum_type.TaskType;
import lombok.Builder;

@Builder
public record TodoTaskSortedResponse (
        Long taskId,
        String title,
        String content,
        String subject,
        TaskType taskType,
        boolean isTaskCompleted,
        boolean isFeedbackCompleted,
        boolean isSubmitted
){
    public static TodoTaskSortedResponse from(TodoTask task) {
        boolean hasSubmission = task.getSubmissions() != null && !task.getSubmissions().isEmpty();
        return TodoTaskSortedResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .content(task.getContent())
                .subject(task.getSubject())
                .taskType(task.getTaskType())
                .isTaskCompleted(task.isCompleted())
                .isFeedbackCompleted(task.getFeedback() != null)
                .isSubmitted(hasSubmission)
                .build();
    }
}
