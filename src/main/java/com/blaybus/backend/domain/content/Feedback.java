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

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Feedback(TodoTask task, MentorProfile mentor, String content) {
        this.task = task;
        this.mentor = mentor;
        this.content = content;
    }

    //피드백 수정 메서드 추가
    public void updateContent(String content) {
        this.content = content;
    }

    //피드백 삭제 메서드 추가
    public void clearContent() {
        this.content = null;
    }

}
