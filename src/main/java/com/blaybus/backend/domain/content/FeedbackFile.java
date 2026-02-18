package com.blaybus.backend.domain.content;

import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackFile extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Builder
    public FeedbackFile(Feedback feedback, String fileName, String fileUrl) {
        this.feedback = feedback;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
