package com.blaybus.backend.domain.notification.controller;

import com.blaybus.backend.domain.notification.dto.response.NotificationResponse;
import com.blaybus.backend.domain.notification.service.NotificationService;
import com.blaybus.backend.global.dto.ApiResponse;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록 조회 (페이지 로드 시 호출)
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(notificationService.getNotifications(userDetails.getUserId()));
    }

    // 알림 읽음 처리 (클릭 시 호출)
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<String> readNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.readNotification(notificationId, userDetails.getUserId());
        return ApiResponse.onSuccess("읽음 처리 되었습니다.");
    }
}