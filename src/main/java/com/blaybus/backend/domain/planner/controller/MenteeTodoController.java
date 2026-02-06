package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.request.MenteeTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.response.MenteeTodoBatchResponse;
import com.blaybus.backend.domain.planner.service.DailyTodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentee/tasks")
public class MenteeTodoController {

    private final DailyTodoService dailyTodoService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<MenteeTodoBatchResponse>> createMenteeTodoBatch(
            @Valid @RequestBody MenteeTodoBatchRequest request
    ) {
        MenteeTodoBatchResponse result =
                dailyTodoService.createMenteeTodoBatch(request);

        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
