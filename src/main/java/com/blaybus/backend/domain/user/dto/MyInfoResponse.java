package com.blaybus.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyInfoResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> MyInfoResponse<T> success(T data) {
        return MyInfoResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }
}