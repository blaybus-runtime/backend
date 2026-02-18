package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.request.CommentRequest;
import com.blaybus.backend.domain.content.dto.request.CommentUpdateRequest;
import com.blaybus.backend.domain.content.dto.response.CommentResponse;
import com.blaybus.backend.domain.content.service.CommentService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성 api
    @PostMapping
    public ApiResponse<String> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentRequest request
    ) {
        commentService.createComment(userDetails.getUserId(), request);
        return ApiResponse.onSuccess("댓글이 성공적으로 등록되었습니다.");
    }

    // 댓글 목록 조회 api
    @GetMapping
    public ApiResponse<List<CommentResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "taskId") Long taskId
    ) {
        List<CommentResponse> comments = commentService.getComments(taskId, userDetails.getUserId());
        return ApiResponse.onSuccess(comments);
    }

    /**
     * [추가] 댓글 수정
     * PATCH /api/v1/comments/{commentId}
     */
    @PatchMapping("/{commentId}")
    public ApiResponse<String> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "commentId") Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        commentService.updateComment(userDetails.getUserId(), commentId, request);
        return ApiResponse.onSuccess("댓글이 수정되었습니다.");
    }

    /**
     * [추가] 댓글 삭제
     * DELETE /api/v1/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "commentId") Long commentId
    ) {
        commentService.deleteComment(userDetails.getUserId(), commentId);
        return ApiResponse.onSuccess("댓글이 삭제되었습니다.");
    }
}
