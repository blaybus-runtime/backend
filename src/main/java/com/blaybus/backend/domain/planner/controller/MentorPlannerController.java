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
     * 멘티의 특정 날짜 할 일 목록 조회 (우선순위 정렬됨)
     * URL: GET /api/v1/mentor/planners/daily?menteeId=3&date=2026-02-01
     */
    @GetMapping("/daily")
    public ApiResponse<List<TodoTaskSortedResponse>> getMenteeTasks(
            @RequestParam(name = "menteeId") Long menteeId,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TodoTaskSortedResponse> response = todoService.getSortedTasksByMenteeAndDate(menteeId, date);
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
