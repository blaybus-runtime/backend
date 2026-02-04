package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.DailyTodoResponseDto;
import com.blaybus.backend.domain.planner.service.DailyTodoService;
import com.blaybus.backend.global.dto.ApiResponse;   // ✅ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
public class DailyTodoController {

    private final DailyTodoService dailyTodoService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyTodoResponseDto>> getDaily(   // ✅ 반환 타입 변경
                                                                         @RequestParam Long menteeId,
                                                                         @RequestParam(required = false)
                                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyTodoResponseDto result = dailyTodoService.getDaily(menteeId, date);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));         // ✅ 감싸서 반환
    }
}
