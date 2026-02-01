package com.blaybus.backend.domain.match;

import com.blaybus.backend.domain.user.MenteeProfile;
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
public class Matching extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private MentorProfile mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id")
    private MenteeProfile mentee;

    @Column(nullable = false)
    private String subject;

    @Builder
    public Matching(MentorProfile mentor, MenteeProfile mentee, String subject) {
        this.mentor = mentor;
        this.mentee = mentee;
        this.subject = subject;
    }
}
