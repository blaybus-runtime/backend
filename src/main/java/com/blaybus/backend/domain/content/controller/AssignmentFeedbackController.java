package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.FeedbackRequest;
import com.blaybus.backend.domain.content.dto.FeedbackResponse;
import com.blaybus.backend.domain.content.service.FeedbackService;
import com.blaybus.backend.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assignments")
public class AssignmentFeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/{assignmentId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> create(
            @PathVariable Long assignmentId,
            @Valid @RequestBody FeedbackRequest.Create request
    ) {
        FeedbackResponse.Create result = feedbackService.createMentorFeedback(assignmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(result));
    }
}
