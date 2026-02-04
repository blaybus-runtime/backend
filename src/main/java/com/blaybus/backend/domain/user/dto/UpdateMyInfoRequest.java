package com.blaybus.backend.domain.user.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class UpdateMyInfoRequest {

    private String name;
    private String profileImage;

    private Profile profile;

    @Getter
    public static class Profile {
        // mentee
        private String phoneNumber;
        private String email;
        private String highSchool;
        private Integer grade;
        private List<String> subjects;
        private String targetUniv;
        private String messageToMentor;

        // mentor
        private String major;
        private String studentIdCard;
        private String bio;
        private Boolean status;
        private String subject;
    }
}
