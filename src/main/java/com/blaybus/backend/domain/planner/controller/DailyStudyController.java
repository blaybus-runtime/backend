package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.DailyStudyResponseDto;
import com.blaybus.backend.domain.planner.service.DailyStudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
public class DailyStudyController {

    private final DailyStudyService dailyStudyService;

    @GetMapping("/daily")
    public ResponseEntity<DailyStudyResponseDto> getDaily(
            @RequestParam Long menteeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(dailyStudyService.getDaily(menteeId, date));
    }
}
