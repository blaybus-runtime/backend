package com.blaybus.backend.domain.notification.repository;

import com.blaybus.backend.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 유저의 알림 목록 (최신순)
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 안 읽은 알림 개수 (뱃지 표시용)
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}