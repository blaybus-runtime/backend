package com.blaybus.backend.domain.user.dto;
import com.blaybus.backend.global.enum_type.Role;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CreateMenteeResponse {
    private UserDto user;
    private String tempPassword; //생성 시 1번만 내려줌
    private MenteeProfileDto menteeProfile;

    @Getter @Builder
    public static class UserDto {
        private Long userId;
        private String username;
        private String name;
        private Role role;
        private String profileImage;
    }

    @Getter @Builder
    public static class MenteeProfileDto {
        private String phoneNumber;
        private String email;
        private String highSchool;
        private Integer grade;
        private List<String> subjects;
        private String messageToMentor;
    }
}
