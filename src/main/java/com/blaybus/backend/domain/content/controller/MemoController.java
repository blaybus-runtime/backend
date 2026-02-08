package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.response.MemoResponse;
import com.blaybus.backend.domain.content.service.MemoService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.blaybus.backend.domain.content.dto.request.MemoRequest;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor")
public class MemoController {

    private final MemoService memoService;

    /**
     * GET /api/v1/mentor/mentees/{menteeId}/memos
     * GET /api/v1/mentor/mentees/{menteeId}/memos?limit=5
     */
    @GetMapping("/mentees/{menteeId}/memos")
    public ApiResponse<MemoResponse.ListResult> getMemos(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long menteeId,
            @RequestParam(required = false) Integer limit
    ) {
        Long mentorId = userDetails.getUserId();

        MemoResponse.ListResult result = (limit == null)
                ? memoService.getMemos(mentorId, menteeId)
                : memoService.getMemosLimit(mentorId, menteeId, limit);

        return ApiResponse.onSuccess(result);
    }

    //메모 작성
    @PostMapping("/mentees/{menteeId}/memos")
    public ApiResponse<MemoResponse.Item> createMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long menteeId,
            @Valid @RequestBody MemoRequest.Create request
    ) {
        Long mentorId = userDetails.getUserId();
        MemoResponse.Item result = memoService.createMemo(mentorId, menteeId, request);
        return ApiResponse.onSuccess(result);
    }


    //메모 수정
    @PutMapping("/memos/{memoId}")
    public ApiResponse<MemoResponse.Item> updateMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memoId,
            @Valid @RequestBody MemoRequest.Update request
    ) {
        Long mentorId = userDetails.getUserId();
        MemoResponse.Item result = memoService.updateMemo(mentorId, memoId, request);
        return ApiResponse.onSuccess(result);
    }


    //메모 삭제
    @DeleteMapping("/memos/{memoId}")
    public ApiResponse<MemoResponse.DeleteResult> deleteMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memoId
    ) {
        Long mentorId = userDetails.getUserId();
        MemoResponse.DeleteResult result = memoService.deleteMemo(mentorId, memoId);
        return ApiResponse.onSuccess(result);
    }



}
