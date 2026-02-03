package com.blaybus.backend.domain.content;

import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyColumn extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private MentorProfile mentor;

    @Column(nullable = false, length = 100)
    private String title; // 제목 (이모지 포함 가능)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 본문 내용

    @Builder
    public StudyColumn(MentorProfile mentor, String title, String content) {
        this.mentor = mentor;
        this.title = title;
        this.content = content;
    }
}
