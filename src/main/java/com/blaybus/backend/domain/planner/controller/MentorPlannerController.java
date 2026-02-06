package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.DailyTodoResponseDto;
import com.blaybus.backend.domain.planner.dto.response.TodoTaskSortedResponse;
import com.blaybus.backend.domain.planner.service.DailyTodoService;
import com.blaybus.backend.domain.planner.service.TodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mentor/planners")
@RequiredArgsConstructor
public class MentorPlannerController {

    private final TodoService todoService;
    private final DailyTodoService dailyTodoService;

    /*
     * 멘티의 특정 날짜(Planner) 할 일 목록 조회 (우선순위 정렬됨)
     * GET /api/v1/mentor/planners/{plannerId}/tasks
     */
    @GetMapping("/{plannerId}/tasks")
    public ApiResponse<List<TodoTaskSortedResponse>> getMenteeTasks(
            @PathVariable(name = "plannerId") Long plannerId
    ) {
        List<TodoTaskSortedResponse> response = todoService.getSortedTasks(plannerId);
        return ApiResponse.onSuccess(response);
    }

    /*
     * 멘토가 특정 멘티의 일일 할 일 목록 조회
     * GET /api/v1/mentor/planners/daily?menteeId={menteeId}&date={yyyy-MM-dd}
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyTodoResponseDto>> getMenteeDailyPlan(
            @RequestParam Long menteeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyTodoResponseDto result = dailyTodoService.getDailyForMentee(menteeId, date);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
