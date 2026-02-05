package com.blaybus.backend.domain.content;

import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import com.blaybus.backend.global.enum_type.MaterialType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Worksheet extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worksheet_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private MentorProfile mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id")
    private MenteeProfile mentee;


    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType materialType; // FILE or BOOK

    private String fileUrl;

    @Builder
    public Worksheet(MentorProfile mentor, MenteeProfile mentee, String title, String subject, MaterialType materialType, String fileUrl) {
        this.mentor = mentor;
        this.mentee = mentee;
        this.title = title;
        this.subject = subject;
        this.materialType = materialType;
        this.fileUrl = fileUrl;
    }
}
