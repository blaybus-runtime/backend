package com.blaybus.backend.domain.planner.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MenteeTodoBatchRequest {

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

    private Long worksheetId;     // optional
    private List<String> weekdays; // optional ("월","화","수","목","금","토","일")
}
