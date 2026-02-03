package com.blaybus.backend.domain.planner.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class StudyProgressResponse {
    private Long menteeId;

    private Period period;       // 조회 기간 (startDate ~ endDate)
    private Summary summary;     // 상단 위젯용 요약 정보
    private List<DailyStat> dailyStats; // 달력에 뿌릴 일별 데이터

    @Getter
    @Builder
    public static class Period {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Getter
    @Builder
    public static class Summary {
        private int totalProgressRate; // 기간 전체 달성률 (불꽃 아이콘 옆)
        private List<SubjectStat> subjectStats; // 과목별 달성률 (링 그래프용)
    }

    @Getter
    @Builder
    public static class SubjectStat {
        private String subject; // 과목명 (국어, 수학 등)
        private int rate;       // 달성률 (0~100)
    }

    @Getter
    @Builder
    public static class DailyStat {
        private LocalDate date;
        private boolean hasTodo;    // 투두가 있는지 여부
        private int progressRate;   // 그날의 달성률
    }
}
