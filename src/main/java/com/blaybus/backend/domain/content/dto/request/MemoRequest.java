package com.blaybus.backend.domain.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class MemoRequest {

    @Getter
    public static class Create {
        @NotBlank(message = "content는 필수입니다.")
        private String content;
    }
}
