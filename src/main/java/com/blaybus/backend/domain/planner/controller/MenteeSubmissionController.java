package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.SubmissionUploadResponseDto;
import com.blaybus.backend.domain.planner.service.SubmissionService;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentee/study/tasks")
public class MenteeSubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(value = "/{taskId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionUploadResponseDto> submitFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        Long menteeId = userDetails.getUserId(); // ✅ 여기
        return ResponseEntity.ok(submissionService.submitFiles(menteeId, taskId, files));
    }
}
