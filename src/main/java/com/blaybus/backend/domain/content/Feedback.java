package com.blaybus.backend.domain.content;

import com.blaybus.backend.domain.planner.TodoTask;
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
public class Feedback extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TodoTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private MentorProfile mentor;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public Feedback(TodoTask task, MentorProfile mentor, String content) {
        this.task = task;
        this.mentor = mentor;
        this.content = content;
    }
}
