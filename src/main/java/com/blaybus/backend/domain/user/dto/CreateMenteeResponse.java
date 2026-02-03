package com.blaybus.backend.domain.user.dto;
import com.blaybus.backend.global.enum_type.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMenteeResponse {
    private Long userId;
    private String username;
    private String name;
    private Role role;      // "MENTEE"
    private String tempPassword; // 1번만
}
