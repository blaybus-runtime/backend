package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.request.TimeRecordRequest;
import com.blaybus.backend.domain.planner.service.StudyPlannerService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
public class StudyPlannerController {

    private final StudyPlannerService studyPlannerService;

    @PostMapping("/time-records")
    public ApiResponse<Long> recordStudyTime(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid TimeRecordRequest request
            ) {
        Long timeRecordId = studyPlannerService.recordStudyTime(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(timeRecordId);
    }

    @DeleteMapping("/time-records/{recordId}")
    public ApiResponse<Void> deleteTimeRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recordId
    ) {
        studyPlannerService.deleteTimeRecord(userDetails.getUserId(), recordId);
        return ApiResponse.onSuccess(null);
    }
}
