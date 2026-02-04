package com.blaybus.backend.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;

    // data가 null 이면 아예 응답에서 빼버림
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // 성공 응답 (data 있는 경우) - 200
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<T>(200, "Success", data);
    }

    // 성공 응답 (data 없는 경우) - 200
    public static <T> ApiResponse<T> onSuccess() {
        return new ApiResponse<T>(200, "Success", null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> onFailure(String message) {
        return new ApiResponse<T>(400, message, null);
    }
}
