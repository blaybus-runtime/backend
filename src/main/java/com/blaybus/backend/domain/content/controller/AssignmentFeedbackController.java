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
            @PathVariable("assignmentId") Long taskId,
            @Valid @RequestBody FeedbackRequest.Create request
    ) {
        FeedbackResponse.Create result = feedbackService.createMentorFeedback(taskId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    //피드백 조회
    @GetMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> get(
            @PathVariable("assignmentId") Long taskId
    ) {
        FeedbackResponse.Create result = feedbackService.getFeedback(taskId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    //피드백 수정
    @PutMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> update(
            @PathVariable("assignmentId") Long taskId,
            @Valid @RequestBody FeedbackRequest.Create request
    ) {
        FeedbackResponse.Create result = feedbackService.updateMentorFeedback(taskId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    //피드백 삭제
    @DeleteMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(
            @PathVariable("assignmentId") Long taskId
    ) {
        feedbackService.deleteMentorFeedback(taskId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

}