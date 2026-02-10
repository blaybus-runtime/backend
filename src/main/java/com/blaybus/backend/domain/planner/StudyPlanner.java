package com.blaybus.backend.domain.planner;

import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "planner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoTask> tasks = new ArrayList<>();

    @Builder
    public StudyPlanner(MenteeProfile mentee, LocalDate planDate, Integer studyTime, String dailyComment) {
        this.mentee = mentee;
        this.planDate = planDate;
        this.studyTime = studyTime;
        this.dailyComment = dailyComment;
    }

    public void addStudyTime(int minutes) {
        if (this.studyTime == null) {
            this.studyTime = 0;
        }
        this.studyTime += minutes;
    }

    public void subtractStudyTime(int minutes) {
        if (this.studyTime == null) {
            this.studyTime = 0;
        }
        this.studyTime = Math.max(0, this.studyTime - minutes);
    }
}
