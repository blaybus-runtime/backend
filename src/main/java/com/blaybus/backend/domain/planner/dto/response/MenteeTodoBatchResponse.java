package com.blaybus.backend.domain.planner.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MenteeTodoBatchResponse {

    private Long menteeId;
    private int createdCount;
    private List<MenteeTodoItem> tasks;

    @Getter
    @Builder
    public static class MenteeTodoItem {
        private Long taskId;
        private LocalDate date;

        private String subject;
        private String goal;
        private String title;

        private String status;     // ex) "UNDONE"
        private String createdBy;  // "MENTEE"
    }
}
