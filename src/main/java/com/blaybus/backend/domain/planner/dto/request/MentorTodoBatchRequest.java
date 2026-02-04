package com.blaybus.backend.domain.planner.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MentorTodoBatchRequest {

    @NotNull
    private Long menteeId;

    @NotBlank
    private String subject; // 한글: "국어", "영어", "수학" ...

    @NotBlank
    private String goal;

    @NotBlank
    private String title;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
