package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse.*;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*; // Map, HashMap, LinkedHashMap 사용을 위해 import
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyProgressService {
    private final DailyStudyPlannerTodoRepository dailyTodoRepository;

    /*
    /api/v1/study/progress/mentorId=1&startDate=2026-02-01&endDate=2026-02-04 담당하는 메소드입니다.
     */
    public Map<String, Object> getProgress(Long menteeId, LocalDate startDate, LocalDate endDate) {

        // 데이터 조회 및 계산 로직 (기존과 동일)
        List<StudyPlanner> planners = dailyTodoRepository.findAllByMentee_UserIdAndPlanDateBetween(menteeId, startDate, endDate);

        Map<LocalDate, StudyPlanner> plannerMap = planners.stream()
                .collect(Collectors.toMap(StudyPlanner::getPlanDate, p -> p));

        List<DailyStat> dailyStats = new ArrayList<>();
        int totalTaskCount = 0;
        int completedTaskCount = 0;
        Map<String, int[]> subjectStatMap = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (plannerMap.containsKey(date)) {
                StudyPlanner planner = plannerMap.get(date);
                List<TodoTask> tasks = planner.getTasks();

                int dailyTotal = tasks.size();
                int dailyDone = 0;

                for (TodoTask task : tasks) {
                    String subject = task.getSubject();
                    subjectStatMap.putIfAbsent(subject, new int[]{0, 0});
                    subjectStatMap.get(subject)[0]++;

                    if (task.isCompleted()) {
                        dailyDone++;
                        subjectStatMap.get(subject)[1]++;
                    }
                }
                totalTaskCount += dailyTotal;
                completedTaskCount += dailyDone;

                int dailyRate = (dailyTotal == 0) ? 0 : (dailyDone * 100 / dailyTotal);
                dailyStats.add(createDailyStat(date, true, dailyRate));
            } else {
                dailyStats.add(createDailyStat(date, false, 0));
            }
        }

        int totalProgressRate = (totalTaskCount == 0) ? 0 : (completedTaskCount * 100 / totalTaskCount);
        List<SubjectStat> subjectStats = createSubjectStats(subjectStatMap);

        // DTO 생성
        StudyProgressResponse data = StudyProgressResponse.builder()
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

        // 순서를 보장하기 위해 LinkedHashMap 사용
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Success");
        response.put("data", data);

        return response;
    }

    // --- 헬퍼 함수 모음입니다 ---
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
            String subject = entry.getKey();
            int total = entry.getValue()[0];
            int done = entry.getValue()[1];
            int rate = (total == 0) ? 0 : (done * 100 / total);
            list.add(SubjectStat.builder().subject(subject).rate(rate).build());
        }
        return list;
    }
}