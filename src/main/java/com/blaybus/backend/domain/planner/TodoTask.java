package com.blaybus.backend.domain.planner;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.global.enum_type.TaskType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.blaybus.backend.domain.content.Feedback;
import org.springframework.scheduling.config.Task;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoTask {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id")
    private StudyPlanner planner;

    // 어떤 학습지인지 (N:1, 자습이면 Null 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worksheet_id")
    private Worksheet worksheet;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String goal;

    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY)
    private Feedback feedback;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();

    @Builder
    public TodoTask(StudyPlanner planner, Worksheet worksheet, String content, String subject, String title, String goal, boolean isCompleted, Integer priority, TaskType taskType) {
        this.planner = planner;
        this.worksheet = worksheet;
        this.content = content;
        this.subject = subject;
        this.title = title;
        this.goal = goal;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.taskType = taskType;
    }

    public void updateCompleted(boolean completed) {
        this.isCompleted = completed;
    }

}


