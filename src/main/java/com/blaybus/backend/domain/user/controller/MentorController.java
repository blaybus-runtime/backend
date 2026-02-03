package com.blaybus.backend.domain.user.controller;

import com.blaybus.backend.domain.user.dto.CreateMenteeRequest;
import com.blaybus.backend.domain.user.dto.CreateMenteeResponse;
import com.blaybus.backend.domain.user.service.MentorMenteeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentors")
public class MentorController {

    private final MentorMenteeService mentorMenteeService;

    @PostMapping("/me/mentees")
    public ResponseEntity<CreateMenteeResponse> createMentee(
            @RequestBody CreateMenteeRequest request
    ) {
        // 지금은 권한 체크 생략
        return ResponseEntity.ok(mentorMenteeService.createMentee(request));
    }
}
