package com.blaybus.backend.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenteeProfile {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String highSchool;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "target_univ")
    private String targetUniv;

    @Column(nullable=false)
    private Integer grade;

    @ElementCollection
    @CollectionTable(name = "mentee_subjects", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "subject")
    private List<String> subjects = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String messageToMentor;

    @Builder
    public MenteeProfile(User user, String phoneNumber, String email, String highSchool,
                         String schoolName, Integer grade, String targetUniv, List<String> subjects, String messageToMentor) {
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.highSchool = highSchool;
        this.schoolName = schoolName != null ? schoolName : (highSchool != null ? highSchool : "");
        this.targetUniv = targetUniv;
        this.grade = grade;
        if (subjects != null) this.subjects = subjects;
        this.messageToMentor = messageToMentor;
    }

    public void updatePhoneNumber(String v){ this.phoneNumber = v; }
    public void updateEmail(String v){ this.email = v; }
    public void updateHighSchool(String v){ this.highSchool = v; }
    public void updateGrade(Integer v){ this.grade = v; }
    public void updateTargetUniv(String v){ this.targetUniv = v; }
    public void updateMessageToMentor(String v){ this.messageToMentor = v; }

    public void replaceSubjects(List<String> newSubjects){
        this.subjects.clear();
        if (newSubjects != null) this.subjects.addAll(newSubjects);
    }

}
