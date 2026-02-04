package com.blaybus.backend.domain.user.controller;

import com.blaybus.backend.domain.user.dto.MyInfoData;
import com.blaybus.backend.domain.user.dto.MyInfoResponse;
import com.blaybus.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse<MyInfoData>> getMe(Authentication authentication) {
        String username = authentication.getName(); // ✅ JwtAuthenticationFilter가 세팅해준 username
        MyInfoData data = userService.getMyInfoByUsername(username);
        return ResponseEntity.ok(MyInfoResponse.success(data));
    }
}
