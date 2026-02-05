package com.blaybus.backend.domain.planner.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeRecordRequest(
        @NotNull(message = "플래너 ID는 필수입니다.")
        Long plannerId,

        @NotNull(message = "과목은 필수입니다.")
        String subject,

        @NotNull(message = "시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime
) {}
