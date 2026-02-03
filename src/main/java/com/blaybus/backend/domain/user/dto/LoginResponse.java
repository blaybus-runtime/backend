package com.blaybus.backend.domain.user.dto;

import com.blaybus.backend.global.enum_type.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {
        private Long userId;
        private String name;
        private String username;
        private Role role;          // "MENTOR" / "MENTEE"
        private String profileImage;
    }
}
