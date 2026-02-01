package com.blaybus.backend.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorProfile {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    private String studentIdCard;

    private String bio;

    @Column(nullable = false)
    private boolean status;

    @Column(nullable = false)
    private String subject;

    @Builder
    public MentorProfile(User user, String major, String studentIdCard, String bio, boolean status, String subject) {
        this.user = user;
        this.major = major;
        this.studentIdCard = studentIdCard;
        this.bio = bio;
        this.status = status;
        this.subject = subject;
    }
}
