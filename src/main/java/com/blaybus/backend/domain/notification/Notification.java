package com.blaybus.backend.domain.notification;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver; // 알림 받는 사람

    @Column(nullable = false)
    private String content; // 알림 내용 (예: "새로운 과제가 등록되었습니다.")

    private String relatedUrl; // 클릭 시 이동할 URL (예: "/tasks/10")

    @Column(nullable = false)
    private boolean isRead; // 읽음 여부 (false: 빨간점, true: 읽음)

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType; // 알림 종류

    @Builder
    public Notification(User receiver, String content, String relatedUrl, NotificationType notificationType) {
        this.receiver = receiver;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.notificationType = notificationType;
        this.isRead = false; // 기본값 안 읽음
    }

    public void read() {
        this.isRead = true;
    }
}