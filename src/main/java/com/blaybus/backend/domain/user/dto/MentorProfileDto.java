package com.blaybus.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MentorProfileDto {
    private String type; // "MENTOR"
    private String major;
    private String studentIdCard;
    private String bio;
    private boolean status;
    private String subject;
}
