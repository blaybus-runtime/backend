package com.blaybus.backend.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenteeProfile {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false)
    private Integer grade;

    private String targetUniv;

    @Builder
    public MenteeProfile(User user, String schoolName, Integer grade, String targetUniv) {
        this.user = user;
        this.schoolName = schoolName;
        this.grade = grade;
        this.targetUniv = targetUniv;
    }
}
