package com.blaybus.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyInfoData {
    private Long userId;
    private String username;
    private String name;
    private String role;
    private String profileImage;
    private Object profile; // MenteeProfileDto or MentorProfileDto
}
