package com.blaybus.backend.domain.planner.dto.request;

import jakarta.validation.constraints.NotEmpty;
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
public class MentorTodoBatchRequest {

    @NotNull
    private Long menteeId;

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

    //  기존 단일 worksheetId 제거(또는 deprecated로 남겨도 됨)
    // private Long worksheetId;

    //  상단 요일(전체 task 생성 요일) - 기존 그대로
    private List<String> weekdays; // optional

    // 파일별 설정
    @NotEmpty
    private List<FileItem> files;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FileItem {
        @NotNull
        private Long worksheetId;
        // 이 파일이 붙을 요일들 (예: ["월","수","금"] or ["MON","WED"])
        // 비워두면 -> 상단 weekdays를 디폴트로 사용하도록 서비스에서 처리
        private List<String> weekdays;
    }
}
