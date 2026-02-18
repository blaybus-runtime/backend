package com.blaybus.backend.domain.planner.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MentorTodoResponse {

    private Long taskId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String subject; // 한글

    private String goal;

    private String title;

    private String status;    // "UNDONE" / "DONE"

    private String createdBy; // "MENTOR"
}
