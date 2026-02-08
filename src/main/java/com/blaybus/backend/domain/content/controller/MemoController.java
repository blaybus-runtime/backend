package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.response.MemoResponse;
import com.blaybus.backend.domain.content.service.MemoService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
