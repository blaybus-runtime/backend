package com.blaybus.backend.domain.planner.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
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
    private String subject;

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

    private Long worksheetId;      // optional
    private List<String> weekdays; // optional

    // ✅ optional: 없거나(null)/비어도([]) 통과
    // ✅ 단, 존재할 경우 내부 FileItem 검증은 유지
    @Valid
    private List<FileItem> files;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FileItem {
        @NotNull
        private Long worksheetId;

        // 비우면 상단 weekdays를 디폴트로 사용
        private List<String> weekdays;
    }
}
