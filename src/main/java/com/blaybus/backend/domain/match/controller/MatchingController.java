package com.blaybus.backend.domain.match.controller;

import com.blaybus.backend.domain.match.dto.response.MenteeCardResponse;
import com.blaybus.backend.domain.match.service.MatchingService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam Long mentorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date
    ) {
        List<MenteeCardResponse> response = matchingService.getMyMentees(mentorId, date);
        return ApiResponse.onSuccess(response);
    }
}
