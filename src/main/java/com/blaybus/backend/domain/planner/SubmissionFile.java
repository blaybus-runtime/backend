package com.blaybus.backend.domain.planner;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Builder
    public SubmissionFile(Submission submission, String fileName, String fileUrl) {
        this.submission = submission;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
