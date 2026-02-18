package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.TaskDetailResponse;
import com.blaybus.backend.domain.planner.service.TodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TodoTaskController {

    private final TodoService todoService;

    /*
     * 할 일 상세 조회 (학습지 + 인증샷 + 피드백)
     * GET /api/v1/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(
            @PathVariable(name = "taskId") Long taskId
    ) {
        TaskDetailResponse response = todoService.getTaskDetail(taskId);
        return ApiResponse.onSuccess(response);
    }

    /**
     * [추가] 해당 Task의 모든 학습지(Worksheet) ZIP 다운로드
     * GET /api/v1/tasks/{taskId}/worksheets/download
     */
    @GetMapping("/{taskId}/worksheets/download")
    public ResponseEntity<StreamingResponseBody> downloadWorksheets(
            @PathVariable(name = "taskId") Long taskId
    ) {
        // 1. [검증] 학습지가 없으면 여기서 에러 발생 -> JSON 응답 나감 (헤더 설정 전이므로 안전!)
        todoService.validateWorksheetsExist(taskId);

        // 2. 다운로드 파일명 설정
        String zipFileName = URLEncoder.encode("학습자료_" + taskId + ".zip", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // 3. 스트리밍 응답 생성 (검증 통과 후에만 실행됨)
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    todoService.downloadWorksheetsAsZip(taskId, outputStream);
                });
    }
}