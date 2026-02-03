package com.blaybus.backend.domain.user.dto;

import lombok.Getter;

@Getter
public class CreateMenteeRequest {
    private String username;
    private String name;
    private MenteeProfileDto menteeProfile;

    @Getter
    public static class MenteeProfileDto {
        private String schoolName;
        private Integer grade;
        private String targetUniv;
    }
}
