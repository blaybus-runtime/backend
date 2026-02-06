package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.TaskDetailResponse;
import com.blaybus.backend.domain.planner.service.TodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}