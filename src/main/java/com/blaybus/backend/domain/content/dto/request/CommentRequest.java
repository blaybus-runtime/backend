package com.blaybus.backend.domain.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotNull(message = "할 일 ID는 필수입니다.")
        Long taskId,

        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content
) {}