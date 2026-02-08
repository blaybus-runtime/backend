package com.blaybus.backend.domain.notification.dto.event;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.global.enum_type.NotificationType;

public record NotificationEvent(
        User receiver,
        String content,
        String url,
        NotificationType type
) {}
