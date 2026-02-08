package com.blaybus.backend.domain.notification.service;

import com.blaybus.backend.domain.notification.Notification;
import com.blaybus.backend.domain.notification.dto.response.NotificationResponse;
import com.blaybus.backend.domain.notification.repository.NotificationRepository;
import com.blaybus.backend.global.exception.GeneralException;
import com.blaybus.backend.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // [핵심] 이벤트를 받아서 알림 저장 (비동기로 처리하면 더 좋음)
    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        Notification notification = Notification.builder()
                .receiver(event.receiver())
                .content(event.content())
                .relatedUrl(event.url())
                .notificationType(event.type())
                .build();

        notificationRepository.save(notification);
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    // 알림 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 본인 알림인지 확인
        if(!notification.getReceiver().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST); // 권한 에러로 변경 권장
        }

        notification.read();
    }
}