package com.blaybus.backend.domain.planner;

import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPlanner extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planner_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id")
    private MenteeProfile mentee;

    @Column(nullable = false)
    private LocalDate planDate;

    private Integer studyTime;

    private String dailyComment;

    @Builder
    public StudyPlanner(MenteeProfile mentee, LocalDate planDate, Integer studyTime, String dailyComment) {
        this.mentee = mentee;
        this.planDate = planDate;
        this.studyTime = studyTime;
        this.dailyComment = dailyComment;
    }
}
