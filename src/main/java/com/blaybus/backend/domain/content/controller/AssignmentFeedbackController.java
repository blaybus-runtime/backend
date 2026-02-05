package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.request.FeedbackRequest;
import com.blaybus.backend.domain.content.dto.response.FeedbackResponse;
import com.blaybus.backend.domain.content.service.FeedbackService;
import com.blaybus.backend.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assignments")
public class AssignmentFeedbackController {

    private final FeedbackService feedbackService;

    //피드백 작성
    @PostMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> create(
            @PathVariable Long assignmentId,
            @Valid @RequestBody FeedbackRequest.Create request
    ) {
        FeedbackResponse.Create result = feedbackService.createMentorFeedback(assignmentId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    //피드백 조회
    @GetMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> get(
            @PathVariable Long assignmentId
    ) {
        FeedbackResponse.Create result = feedbackService.getFeedback(assignmentId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    //피드백 수정
    @PutMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> update(
            @PathVariable Long assignmentId,
            @Valid @RequestBody FeedbackRequest.Create request
    ) {
        FeedbackResponse.Create result =
                feedbackService.updateMentorFeedback(assignmentId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }


}