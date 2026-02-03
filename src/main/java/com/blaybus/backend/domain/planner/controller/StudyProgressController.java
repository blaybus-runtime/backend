package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse;
import com.blaybus.backend.domain.planner.service.StudyProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
public class StudyProgressController {

    private final StudyProgressService studyProgressService;

    /*
     월간/주간 학습 진척도 조회 API
     예시: GET /api/v1/study/progress?menteeId=2&startDate=2026-02-01&endDate=2026-02-28
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getStudyProgress( // 👈 여기 타입 변경
                                                                 @RequestParam Long menteeId,
                                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        // 👈 서비스가 이미 Map을 반환하므로 그대로 리턴
        Map<String, Object> response = studyProgressService.getProgress(menteeId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
