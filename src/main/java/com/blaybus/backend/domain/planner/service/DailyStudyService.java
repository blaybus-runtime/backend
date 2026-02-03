package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.DailyStudyResponseDto;
import com.blaybus.backend.domain.planner.repository.StudyPlannerRepository;
import com.blaybus.backend.domain.planner.repository.TodoTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStudyService {

    private final StudyPlannerRepository studyPlannerRepository;
    private final TodoTaskRepository todoTaskRepository;

    public DailyStudyResponseDto getDaily(Long menteeId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        Optional<StudyPlanner> plannerOpt =
                studyPlannerRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, targetDate);

        // 플래너 자체가 없으면 빈 값 반환 (프론트가 안전하게 처리 가능)
        if (plannerOpt.isEmpty()) {
            return DailyStudyResponseDto.builder()
                    .menteeId(menteeId)
                    .date(targetDate)
                    .progress(DailyStudyResponseDto.ProgressDto.builder()
                            .completionRate(0)
                            .bySubject(Collections.emptyList())
                            .build())
                    .todos(Collections.emptyList())
                    .build();
        }

        StudyPlanner planner = plannerOpt.get();
        List<TodoTask> tasks = todoTaskRepository.findAllByPlanner_IdOrderByPriorityAscIdAsc(planner.getId());

        // 1) 전체 completionRate
        int total = tasks.size();
        long done = tasks.stream().filter(TodoTask::isCompleted).count();
        int completionRate = (total == 0) ? 0 : (int) Math.round(done * 100.0 / total);

        // 2) 과목별 percent (해당 날짜의 todo만 기준)
        Map<String, List<TodoTask>> bySubjectMap = tasks.stream()
                .collect(Collectors.groupingBy(TodoTask::getSubject));

        List<DailyStudyResponseDto.SubjectProgressDto> bySubject = bySubjectMap.entrySet().stream()
                .map(entry -> {
                    String subject = entry.getKey();
                    List<TodoTask> subjectTasks = entry.getValue();
                    int subjectTotal = subjectTasks.size();
                    long subjectDone = subjectTasks.stream().filter(TodoTask::isCompleted).count();
                    int percent = (subjectTotal == 0) ? 0 : (int) Math.round(subjectDone * 100.0 / subjectTotal);

                    return DailyStudyResponseDto.SubjectProgressDto.builder()
                            .subject(subject)
                            .percent(percent)
                            .build();
                })
                // 정렬은 일단 subject 문자열 기준 (UI에서 정렬 필요하면 바꿔줄게)
                .sorted(Comparator.comparing(DailyStudyResponseDto.SubjectProgressDto::getSubject))
                .toList();

        // 3) todos dto
        List<DailyStudyResponseDto.TodoDto> todoDtos = tasks.stream()
                .map(t -> DailyStudyResponseDto.TodoDto.builder()
                        .id(t.getId())
                        .content(t.getContent())
                        .subject(t.getSubject())
                        .isCompleted(t.isCompleted())
                        .priority(t.getPriority())
                        .taskType(t.getTaskType().name())
                        .build())
                .toList();

        return DailyStudyResponseDto.builder()
                .menteeId(menteeId)
                .date(targetDate)
                .progress(DailyStudyResponseDto.ProgressDto.builder()
                        .completionRate(completionRate)
                        .bySubject(bySubject)
                        .build())
                .todos(todoDtos)
                .build();
    }
}
