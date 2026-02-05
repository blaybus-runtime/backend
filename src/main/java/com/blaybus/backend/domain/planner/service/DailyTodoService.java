package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
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

import java.time.DayOfWeek;
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

    @Transactional // write 트랜잭션
    public MentorTodoBatchResponse createMentorTodoBatch(Long mentorId, MentorTodoBatchRequest req) {

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("startDate는 endDate보다 늦을 수 없습니다.");
        }

        Long menteeId = req.getMenteeId();

        MenteeProfile mentee = em.find(MenteeProfile.class, menteeId);
        if (mentee == null) {
            throw new IllegalArgumentException("유효하지 않은 menteeId 입니다.");
        }

        // ✅ worksheetId(optional) 처리: 있으면 참조 프록시 가져오기 (DB hit 최소화)
        Worksheet worksheetRef = null;
        if (req.getWorksheetId() != null) {
            worksheetRef = em.getReference(Worksheet.class, req.getWorksheetId());
        }

        // ✅ weekdays(optional) 처리: 있으면 해당 요일에만 생성
        Set<DayOfWeek> selectedDays = parseWeekdays(req.getWeekdays());

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

            // ✅ 선택 요일이 있으면, 해당 요일만 생성
            if (!selectedDays.isEmpty() && !selectedDays.contains(d.getDayOfWeek())) {
                d = d.plusDays(1);
                continue;
            }

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

            // ✅ worksheetRef가 있으면 각 TodoTask에 동일하게 세팅
            TodoTask saved = todoRepository.save(
                    TodoTask.builder()
                            .planner(planner)
                            .worksheet(worksheetRef)
                            .content(req.getTitle() + " | " + req.getGoal())
                            .subject(req.getSubject())
                            .title(req.getTitle())
                            .goal(req.getGoal())
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

    /**
     * ✅ 프론트 요일 값이 한글(일~토)로 오든, 영문(SUN~SAT)로 오든 다 처리
     * - weekdays가 null/empty면 "매일 생성"을 의미하도록 빈 Set 반환
     */
    private Set<DayOfWeek> parseWeekdays(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) return Collections.emptySet();

        Set<DayOfWeek> result = new HashSet<>();
        for (String w : weekdays) {
            if (w == null) continue;
            String s = w.trim().toUpperCase();

            switch (s) {
                // 한글 요일
                case "일": result.add(DayOfWeek.SUNDAY); break;
                case "월": result.add(DayOfWeek.MONDAY); break;
                case "화": result.add(DayOfWeek.TUESDAY); break;
                case "수": result.add(DayOfWeek.WEDNESDAY); break;
                case "목": result.add(DayOfWeek.THURSDAY); break;
                case "금": result.add(DayOfWeek.FRIDAY); break;
                case "토": result.add(DayOfWeek.SATURDAY); break;

                // 영문 요일 코드
                case "SUN": result.add(DayOfWeek.SUNDAY); break;
                case "MON": result.add(DayOfWeek.MONDAY); break;
                case "TUE": result.add(DayOfWeek.TUESDAY); break;
                case "WED": result.add(DayOfWeek.WEDNESDAY); break;
                case "THU": result.add(DayOfWeek.THURSDAY); break;
                case "FRI": result.add(DayOfWeek.FRIDAY); break;
                case "SAT": result.add(DayOfWeek.SATURDAY); break;

                default:
                    throw new IllegalArgumentException("요일 값이 올바르지 않습니다: " + w);
            }
        }
        return result;
    }
}
