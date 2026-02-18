package com.blaybus.backend.global.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {

    // [Common]
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의하세요"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),

    // [User]
    _USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자를 찾을 수 없습니다."),

    // [Task/Planner]
    // ★ 여기에 우리가 필요한 에러를 정의합니다.
    _TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK4001", "해당 할 일을 찾을 수 없습니다."),
    _PLANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "PLANNER4001", "해당 플래너를 찾을 수 없습니다."),

    _WORKSHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK4002", "다운로드할 학습지가 없습니다."),

    _COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4001", "해당 댓글을 찾을 수 없습니다."),
    _COMMENT_NOT_WRITER(HttpStatus.FORBIDDEN, "COMMENT4002", "댓글 작성자만 수정/삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}