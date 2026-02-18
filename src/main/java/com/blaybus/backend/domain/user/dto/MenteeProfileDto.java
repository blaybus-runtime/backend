package com.blaybus.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenteeProfileDto {
    private String type; // "MENTEE"
    private String phoneNumber;
    private String email;
    private String highSchool;
    private Integer grade;
    private List<String> subjects;
    private String targetUniv;
    private String messageToMentor;
}
