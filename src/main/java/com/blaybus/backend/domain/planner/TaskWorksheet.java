package com.blaybus.backend.domain.planner;

import com.blaybus.backend.domain.content.Worksheet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "task_worksheet",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_task_worksheet", columnNames = {"task_id", "worksheet_id"})
        }
)
public class TaskWorksheet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_worksheet_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TodoTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worksheet_id", nullable = false)
    private Worksheet worksheet;

    /**
     * 파일별 반복 요일 저장 (예: "MON,WED,FRI")
     * - null/blank 가능: "상단에서 지정 안했거나, 매일" 같은 정책을 나중에 정하면 됨
     */
    @Column(name = "weekdays", length = 32)
    private String weekdays;

    @Builder
    public TaskWorksheet(TodoTask task, Worksheet worksheet, String weekdays) {
        this.task = task;
        this.worksheet = worksheet;
        this.weekdays = weekdays;
    }

    public void updateWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }
}
