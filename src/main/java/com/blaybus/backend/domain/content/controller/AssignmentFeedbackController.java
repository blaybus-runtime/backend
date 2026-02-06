package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.request.FeedbackRequest;
import com.blaybus.backend.domain.content.dto.response.FeedbackResponse;
import com.blaybus.backend.domain.content.service.FeedbackService;
import com.blaybus.backend.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assignments")
public class AssignmentFeedbackController {

    private final FeedbackService feedbackService;

    //피드백 작성
//    @PostMapping("/{assignmentId}/feedback")
//    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> create(
//            @PathVariable("assignmentId") Long taskId,
//            @Valid @RequestBody FeedbackRequest.Create request
//    ) {
//        FeedbackResponse.Create result = feedbackService.createMentorFeedback(taskId, request);
//        return ResponseEntity.ok(ApiResponse.onSuccess(result));
//    }

    //피드백 + 파일 업로드
    @PostMapping(value = "/{assignmentId}/feedback", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FeedbackResponse.Create>> create(
            @PathVariable("assignmentId") Long taskId,
            @RequestPart(value = "content") String content,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        List<MultipartFile> fileList = (files == null) ? List.of() : List.of(files);
        FeedbackResponse.Create result = feedbackService.createMentorFeedbackWithFiles(taskId, content, fileList);
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


    //파일 다운로드
    @GetMapping("/{assignmentId}/feedback/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable("assignmentId") Long taskId,
            @PathVariable("fileId") Long fileId
    ) {
        return feedbackService.downloadFeedbackFile(taskId, fileId);
    }

    //파일 삭제
    @DeleteMapping("/{assignmentId}/feedback/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable("assignmentId") Long taskId,
            @PathVariable("fileId") Long fileId
    ) {
        feedbackService.deleteFeedbackFile(taskId, fileId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

}