package com.blaybus.backend.domain.content.dto.request;

import jakarta.validation.constraints.NotBlank;

public class FeedbackRequest {

    public record Create(
            @NotBlank(message = "content는 필수입니다.")
            String content
    ) {}
}
