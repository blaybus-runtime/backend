package com.blaybus.backend.domain.user.controller;

import com.blaybus.backend.domain.user.dto.CreateMenteeRequest;
import com.blaybus.backend.domain.user.dto.CreateMenteeResponse;
import com.blaybus.backend.domain.user.service.MentorMenteeService;
import com.blaybus.backend.global.security.CustomUserDetails;
import com.blaybus.backend.global.enum_type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentors")
public class MentorController {

    private final MentorMenteeService mentorMenteeService;

    @PostMapping("/me/mentees")
    public ResponseEntity<CreateMenteeResponse> createMentee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateMenteeRequest request
    ) {
        // 로그인 안 했으면
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        // 멘토만 가능
        if (userDetails.getRole() != Role.MENTOR) {
            throw new IllegalArgumentException("멘토만 멘티를 생성할 수 있습니다.");
        }

        Long mentorId = userDetails.getUserId(); // (다음 단계 match 저장할 때 쓸 값)

        return ResponseEntity.ok(mentorMenteeService.createMentee(mentorId, request));
    }
}
