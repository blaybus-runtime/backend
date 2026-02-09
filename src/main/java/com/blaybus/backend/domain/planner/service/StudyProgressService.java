package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse;
import com.blaybus.backend.domain.planner.dto.response.StudyProgressResponse.*;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import com.blaybus.backend.domain.planner.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyProgressService {
    private final DailyStudyPlannerTodoRepository dailyTodoRepository;
    private final SubmissionRepository submissionRepository;

    /*
    /api/v1/study/progress 담당하는 메소드입니다.
    - 김동수 -
     */
    public StudyProgressResponse getProgress(Long menteeId, LocalDate startDate, LocalDate endDate) {

        // menteeId와 일치하는 멘티의 플래너 중 startDate와 endDate 사이의 플래너만 가져옴
        List<StudyPlanner> planners = dailyTodoRepository.findAllByMentee_UserIdAndPlanDateBetween(menteeId, startDate, endDate);

        // ==========================================
        // ▼ [추가] 조회된 플래너들에 포함된 모든 Task ID 추출
        // ==========================================
        List<Long> allTaskIds = planners.stream()
                .flatMap(p -> p.getTasks().stream())
                .map(TodoTask::getId)
                .toList();

        // ▼ [추가] 해당 Task들 중, 실제로 Submission이 존재하는 Task ID만 조회 (Set으로 저장하여 검색 속도 O(1))
        Set<Long> submittedTaskIds = new HashSet<>();
        if (!allTaskIds.isEmpty()) {
            submittedTaskIds = submissionRepository.findTaskIdsByMenteeIdAndTaskIds(menteeId, allTaskIds);
        }
        // ==========================================

        // 날짜-플래너 조회용 map 하나 생성
        Map<LocalDate, StudyPlanner> plannerMap = planners.stream()
                .collect(Collectors.toMap(StudyPlanner::getPlanDate, p -> p));

        List<DailyStat> dailyStats = new ArrayList<>(); // 날짜 별 통계치를 저장할 리스트
        int totalTaskCount = 0; // 총 todo 개수를 저장할 변수
        int completedTaskCount = 0; // 완료한 todo 개수를 저장할 변수
        Map<String, int[]> subjectStatMap = new HashMap<>(); // 과목 별 통계치를 저장할 용도의 map (0번째는 개수 카운팅, 1번째는 완료 개수 카운팅)


        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 현재 날짜에 플래너가 있는 경우
            if (plannerMap.containsKey(date)) {
                StudyPlanner planner = plannerMap.get(date);
                List<TodoTask> tasks = planner.getTasks();

                int dailyTotal = tasks.size(); // 현재 날짜의 todo 개수
                int dailyDone = 0; // 현재 날짜의 todo 중 완료된 todo 개수

                for (TodoTask task : tasks) {
                    String subject = task.getSubject();
                    subjectStatMap.putIfAbsent(subject, new int[]{0, 0}); // 처음으로 map에 추가하는 거면 초기화
                    subjectStatMap.get(subject)[0]++; // 개수 카운팅

                    // ==========================================
                    // ▼ [변경] task.isCompleted() 대신 제출 내역 존재 여부로 판단
                    // ==========================================
                    boolean isDone = submittedTaskIds.contains(task.getId());

                    // 완료한 todo 개수 카운팅
                    if (isDone) { // (기존: if (task.isCompleted()))
                        dailyDone++;
                        subjectStatMap.get(subject)[1]++;
                    }
                }

                // 전체 통계 업데이트
                totalTaskCount += dailyTotal;
                completedTaskCount += dailyDone;

                // 현재 날짜의 진척도 계산
                int dailyRate = (dailyTotal == 0) ? 0 : (dailyDone * 100 / dailyTotal);
                dailyStats.add(createDailyStat(date, true, dailyRate));
            } else {
                // 현재 날짜의 플래너가 없는 경우 빈껍데기 데이터 추가
                dailyStats.add(createDailyStat(date, false, 0));
            }
        }

        // startDate ~ endDate 기간 동안의 전체 진척도 계산
        int totalProgressRate = (totalTaskCount == 0) ? 0 : (completedTaskCount * 100 / totalTaskCount);
        List<SubjectStat> subjectStats = createSubjectStats(subjectStatMap);

        // DTO 생성
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

    // --------------------------- 여기부터 헬퍼 함수 모음입니다 ---------------------------
    // DailyStat DTO 생성 헬퍼 함수
    private DailyStat createDailyStat(LocalDate date, boolean hasTodo, int rate) {
        return DailyStat.builder()
                .date(date)
                .hasTodo(hasTodo)
                .progressRate(rate)
                .build();
    }

    // SubjectStat DTO 생성 헬퍼 함수
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