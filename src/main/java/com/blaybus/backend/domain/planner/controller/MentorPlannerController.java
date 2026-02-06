package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.TodoTaskSortedResponse;
import com.blaybus.backend.domain.planner.service.TodoService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mentor/planners")
@RequiredArgsConstructor
public class MentorPlannerController {

    private final TodoService todoService;

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
}
