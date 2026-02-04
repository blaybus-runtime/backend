package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.request.MentorTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.response.DailyTodoResponseDto;
import com.blaybus.backend.domain.planner.dto.response.MentorTodoBatchResponse;
import com.blaybus.backend.domain.planner.dto.response.MentorTodoResponse;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.global.enum_type.TaskType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTodoService {

    private final DailyStudyPlannerTodoRepository studyPlannerRepository;
    private final TodoRepository todoRepository;

    @PersistenceContext
    private EntityManager em;

    public DailyTodoResponseDto getDaily(Long menteeId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        Optional<StudyPlanner> plannerOpt =
                studyPlannerRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, targetDate);

        if (plannerOpt.isEmpty()) {
            return DailyTodoResponseDto.builder()
                    .menteeId(menteeId)
                    .date(targetDate)
                    .todos(Collections.emptyList())
                    .build();
        }

        StudyPlanner planner = plannerOpt.get();
        List<TodoTask> tasks = todoRepository.findAllByPlanner_IdOrderByPriorityAscIdAsc(planner.getId());

        List<DailyTodoResponseDto.TodoDto> todoDtos = tasks.stream()
                .map(t -> DailyTodoResponseDto.TodoDto.builder()
                        .id(t.getId())
                        .content(t.getContent())
                        .subject(t.getSubject())
                        .isCompleted(t.isCompleted())
                        .priority(t.getPriority())
                        .taskType(t.getTaskType().name())
                        .build())
                .toList();

        return DailyTodoResponseDto.builder()
                .menteeId(menteeId)
                .date(targetDate)
                .todos(todoDtos)
                .build();
    }

    @Transactional // ✅ write 트랜잭션
    public MentorTodoBatchResponse createMentorTodoBatch(Long mentorId, MentorTodoBatchRequest req) {

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("startDate는 endDate보다 늦을 수 없습니다.");
        }

        Long menteeId = req.getMenteeId();

        MenteeProfile mentee = em.find(MenteeProfile.class, menteeId);
        if (mentee == null) {
            throw new IllegalArgumentException("유효하지 않은 menteeId 입니다.");
        }

        List<StudyPlanner> existingPlanners =
                studyPlannerRepository.findAllByMentee_UserIdAndPlanDateBetween(
                        menteeId, req.getStartDate(), req.getEndDate()
                );

        Map<LocalDate, StudyPlanner> plannerMap = new HashMap<>();
        for (StudyPlanner p : existingPlanners) {
            plannerMap.put(p.getPlanDate(), p);
        }

        List<MentorTodoResponse> created = new ArrayList<>();

        LocalDate d = req.getStartDate();
        while (!d.isAfter(req.getEndDate())) {

            StudyPlanner planner = plannerMap.get(d);

            if (planner == null) {
                planner = studyPlannerRepository.save(
                        StudyPlanner.builder()
                                .mentee(mentee)
                                .planDate(d)
                                .studyTime(null)
                                .dailyComment(null)
                                .build()
                );
                plannerMap.put(d, planner);
            }

            // ✅ SQL 에러 해결: title과 goal 필드를 모두 추가했습니다.
            TodoTask saved = todoRepository.save(
                    TodoTask.builder()
                            .planner(planner)
                            .worksheet(null)
                            .content(req.getTitle() + " | " + req.getGoal())
                            .subject(req.getSubject())
                            .title(req.getTitle()) // 👈 추가된 부분
                            .goal(req.getGoal())   // 👈 추가된 부분
                            .isCompleted(false)
                            .priority(1)
                            .taskType(TaskType.ASSIGNMENT)
                            .build()
            );

            created.add(MentorTodoResponse.builder()
                    .taskId(saved.getId())
                    .date(d)
                    .subject(req.getSubject())
                    .title(req.getTitle())
                    .goal(req.getGoal())
                    .status("UNDONE")
                    .createdBy("MENTOR")
                    .build());

            d = d.plusDays(1);
        }

        return MentorTodoBatchResponse.builder()
                .menteeId(menteeId)
                .createdCount(created.size())
                .tasks(created)
                .build();
    }
}