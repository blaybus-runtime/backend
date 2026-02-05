package com.blaybus.backend.domain.content.dto.response;

import com.blaybus.backend.domain.content.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String writerName,
        String writerProfileUrl,
        String content,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt, // 작성 시간

        boolean isMine // 내가 쓴 댓글인지 여부 (수정/삭제 버튼 표시용)
) {
    // Entity -> DTO 변환 편의 메서드
    public static CommentResponse from(Comment comment, Long currentUserId) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getName(),
                comment.getUser().getProfileImage(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getId().equals(currentUserId)
        );
    }
}
