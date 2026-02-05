package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.request.TaskStatusUpdateRequestDto;
import com.blaybus.backend.domain.planner.dto.response.TaskStatusUpdateResponseDto;
import com.blaybus.backend.domain.planner.service.TodoStatusService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
public class TodoStatusController {

    private final TodoStatusService todoStatusService;

    /**
     * TODO 완료 상태 변경
     * (임시) 로그인 미구현 상태라 menteeId를 쿼리로 받아 소유권 검증용으로 사용
     *
     * PATCH /api/v1/study/tasks/{taskId}?menteeId=1
     * Body: { "isCompleted": true }
     */
    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskStatusUpdateResponseDto>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Long menteeId,
            @RequestBody TaskStatusUpdateRequestDto requestDto
    ) {
        TaskStatusUpdateResponseDto result =
                todoStatusService.updateTaskStatus(taskId, menteeId, requestDto);

        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
