package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse.*;
import com.blaybus.backend.domain.planner.repository.DailyTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyProgressService {
    private final DailyTodoRepository dailyTodoRepository;

    public StudyProgressResponse getProgress(Long menteeId, LocalDate startDate, LocalDate endDate) {

        // 현재 멘티의 startDate ~ endDate 사이 StudyPlanner를 모두 가져옴
        List<StudyPlanner> planners = dailyTodoRepository.findAllByMentee_UserIdAndPlanDateBetween(menteeId, startDate, endDate);

        // planner 리스트를 map 으로 변환
        Map<LocalDate, StudyPlanner> plannerMap = planners.stream()
                .collect(Collectors.toMap(StudyPlanner::getPlanDate, p -> p));

        // 결과를 담을 리스트 생성
        List<DailyStat> dailyStats = new ArrayList<>();

        int totalTaskCount = 0;      // 기간 내 총 할 일 개수
        int completedTaskCount = 0;  // 기간 내 완료한 개수

        // 과목별 통계 임시 저장소 (Key: 과목명, Value: [총개수, 완료개수])
        Map<String, int[]> subjectStatMap = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            if (plannerMap.containsKey(date)) {
                // 플래너가 있는 날짜 처리
                StudyPlanner planner = plannerMap.get(date);
                List<TodoTask> tasks = planner.getTasks(); // @OneToMany 관계된 리스트 가져오기

                int dailyTotal = tasks.size(); // 현재 날짜의 task 개수
                int dailyDone = 0; // 현재 날짜에서 완료된 task 개수

                for (TodoTask task : tasks) {
                    // 과목별 통계 누적
                    String subject = task.getSubject();
                    subjectStatMap.putIfAbsent(subject, new int[]{0, 0}); // 없으면 초기화
                    subjectStatMap.get(subject)[0]++; // 총 개수 +1

                    if (task.isCompleted()) {
                        dailyDone++;
                        subjectStatMap.get(subject)[1]++; // 완료 개수 +1
                    }
                }

                // 전체 통계 누적
                totalTaskCount += dailyTotal;
                completedTaskCount += dailyDone;

                // 일별 달성률 계산 (분모가 0이면 0%)
                int dailyRate = (dailyTotal == 0) ? 0 : (dailyDone * 100 / dailyTotal);
                dailyStats.add(createDailyStat(date, true, dailyRate));

            } else {
                // 플래너가 없는 날의 경우 빈 껍데기 객체 생성
                dailyStats.add(createDailyStat(date, false, 0));
            }
        }

        // 전체 기간 평균 달성률 계산
        int totalProgressRate = (totalTaskCount == 0) ? 0 : (completedTaskCount * 100 / totalTaskCount);

        // 과목별 통계 Map -> List 변환
        List<SubjectStat> subjectStats = createSubjectStats(subjectStatMap);

        // 최종 Response DTO 빌드 및 반환
        return StudyProgressResponse.builder()
                .menteeId(menteeId)
                .period(Period.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .summary(Summary.builder()
                        .totalProgressRate(totalProgressRate)
                        .subjectStats(subjectStats)
                        .build())
                .dailyStats(dailyStats)
                .build();
    }

    // --- 여기부터 헬퍼 함수 모음입니당 ---

    private DailyStat createDailyStat(LocalDate date, boolean hasTodo, int rate) {
        return DailyStat.builder()
                .date(date)
                .hasTodo(hasTodo)
                .progressRate(rate)
                .build();
    }

    private List<SubjectStat> createSubjectStats(Map<String, int[]> map) {
        List<SubjectStat> list = new ArrayList<>();

        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            String subject = entry.getKey();     // 과목명 (예: "KOREAN")
            int total = entry.getValue()[0];     // 총 개수
            int done = entry.getValue()[1];      // 완료 개수

            int rate = (total == 0) ? 0 : (done * 100 / total);

            list.add(SubjectStat.builder()
                    .subject(subject)
                    .rate(rate)
                    .build());
        }
        return list;
    }
}
