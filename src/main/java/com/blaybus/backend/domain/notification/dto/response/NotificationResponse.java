package com.blaybus.backend.domain.notification.dto.response;

import com.blaybus.backend.domain.notification.Notification;

public record NotificationResponse(
        Long id,
        String content,
        String url,
        boolean isRead,
        String type,
        String createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getContent(),
                n.getRelatedUrl(),
                n.isRead(),
                n.getNotificationType().name(),
                n.getCreatedAt().toString() // 포맷팅 필요시 수정
        );
    }
}
