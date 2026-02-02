package com.blaybus.backend.domain.planner;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id")
    private StudyPlanner planner;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Builder
    public TimeRecord(StudyPlanner planner, String subject, LocalTime startTime, LocalTime endTime) {
        this.planner = planner;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
