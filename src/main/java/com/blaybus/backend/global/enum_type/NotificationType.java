package com.blaybus.backend.global.enum_type;

public enum NotificationType {
    NEW_TODO,       // 멘토가 할 일 추가
    NEW_WORKSHEET,  // 학습지 등록
    NEW_COMMENT,    // 댓글 달림
    SUBMISSION,     // 멘티가 과제 제출
    NEW_FEEDBACK,       // [추가] 멘토가 피드백 등록함 (멘티에게 알림)
    FEEDBACK_REMINDER // 피드백 재촉 (스케줄러)
}