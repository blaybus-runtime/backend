package com.blaybus.backend.global.enum_type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    MENTOR("ROLE_MENTOR", "멘토"),
    MENTEE("ROLE_MENTEE", "멘티");

    private final String key;
    private final String title;
}
