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
     * PATCH /api/v1/study/tasks/{taskId}?menteeId=1
     * Body: { "isCompleted": true }
     */
    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskStatusUpdateResponseDto>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody TaskStatusUpdateRequestDto requestDto
    ) {
        TaskStatusUpdateResponseDto result =
                todoStatusService.updateTaskStatus(taskId, requestDto);

        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
