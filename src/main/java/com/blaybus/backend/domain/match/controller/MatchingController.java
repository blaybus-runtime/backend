package com.blaybus.backend.domain.match.controller;

import com.blaybus.backend.domain.match.dto.response.MenteeCardResponse;
import com.blaybus.backend.domain.match.service.MatchingService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/matchings")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    public ApiResponse<List<MenteeCardResponse>> getMyMentees(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date
    ) {
        if(userDetails.getRole() != Role.MENTOR) {
            return ApiResponse.onFailure("멘토만 접근할 수 있습니다.");
        }

        Long currentUserId = userDetails.getUserId();

        List<MenteeCardResponse> response = matchingService.getMyMentees(currentUserId, date);
        return ApiResponse.onSuccess(response);
    }
}
