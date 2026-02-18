package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.request.MentorTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.response.MentorTodoBatchResponse;
import com.blaybus.backend.domain.planner.service.DailyTodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor/tasks")
public class MentorTodoController {

    private final DailyTodoService dailyTodoService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<MentorTodoBatchResponse>> createMentorTodo(
            @Valid @RequestBody MentorTodoBatchRequest request
    ) {
        System.out.println("✅ MentorTodoController /batch HIT");

        MentorTodoBatchResponse result = dailyTodoService.createMentorTodoBatch(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
