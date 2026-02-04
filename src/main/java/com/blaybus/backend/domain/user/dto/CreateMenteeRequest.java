package com.blaybus.backend.domain.user.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class CreateMenteeRequest {

    private String name;
    private MenteeProfileDto menteeProfile;

    @Getter
    public static class MenteeProfileDto {
        private String phoneNumber;
        private String email;
        private String highSchool;
        private Integer grade;
        private List<String> Subjects; // ["국어","수학","영어"]
        private String messageToMentor;
    }
}
