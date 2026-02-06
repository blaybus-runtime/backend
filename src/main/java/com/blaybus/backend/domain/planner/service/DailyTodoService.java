package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.request.MenteeTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.request.MentorTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.response.DailyTodoResponseDto;
import com.blaybus.backend.domain.planner.dto.response.MenteeTodoBatchResponse;
import com.blaybus.backend.domain.planner.dto.response.MentorTodoBatchResponse;
import com.blaybus.backend.domain.planner.dto.response.MentorTodoResponse;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.enum_type.TaskType;
import com.blaybus.backend.global.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTodoService {

    private final DailyStudyPlannerTodoRepository studyPlannerRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    public DailyTodoResponseDto getDaily(LocalDate date) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        Long menteeId = user.getId();
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
                .build(); //여기
    }

    @Transactional
    public MenteeTodoBatchResponse createMenteeTodoBatch(MenteeTodoBatchRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        System.out.println("🔍 현재 로그인 유저: " + username);
        System.out.println("🔍 유저의 Role: [" + user.getRole() + "]");

        if (user.getRole() != Role.MENTEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘티 권한이 필요합니다.");
        }

        return generateTodoBatch(user.getId(), request.getStartDate(), request.getEndDate(),
                request.getWeekdays(), request.getSubject(), request.getTitle(),
                request.getGoal(), request.getWorksheetId(), "MENTEE");
    }

    @Transactional
    public MentorTodoBatchResponse createMentorTodoBatch(MentorTodoBatchRequest req) {
        String username = SecurityUtils.getCurrentUsername();
        User mentorUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (mentorUser.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 과제를 생성할 수 있습니다.");
        }

        MenteeTodoBatchResponse result = generateTodoBatch(req.getMenteeId(), req.getStartDate(), req.getEndDate(),
                req.getWeekdays(), req.getSubject(), req.getTitle(),
                req.getGoal(), req.getWorksheetId(), "MENTOR");

        return MentorTodoBatchResponse.builder()
                .menteeId(req.getMenteeId())
                .createdCount(result.getCreatedCount())
                .tasks(result.getTasks().stream()
                        .map(t -> MentorTodoResponse.builder()
                                .taskId(t.getTaskId())
                                .date(t.getDate())
                                .subject(t.getSubject())
                                .title(t.getTitle())
                                .goal(t.getGoal())
                                .status(t.getStatus())
                                .createdBy("MENTOR")
                                .build())
                        .toList())
                .build();
    }

    // 메서드 중복 제거: 하나만 남김
    private MenteeTodoBatchResponse generateTodoBatch(Long menteeUserId, LocalDate startDate, LocalDate endDate,
                                                      List<String> weekdays, String subject, String title,
                                                      String goal, Long worksheetId, String creatorRole) {

        // DB에 MenteeProfile이 있는지 먼저 확인
        MenteeProfile mentee = em.find(MenteeProfile.class, menteeUserId);
        if (mentee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "멘티 프로필(ID: " + menteeUserId + ")이 존재하지 않습니다.");
        }

        Worksheet worksheetRef = null;
        if (worksheetId != null) {
            worksheetRef = em.find(Worksheet.class, worksheetId);
        }

        Set<DayOfWeek> selectedDays = parseWeekdays(weekdays);
        List<StudyPlanner> existingPlanners = studyPlannerRepository.findAllByMentee_UserIdAndPlanDateBetween(
                menteeUserId, startDate, endDate);

        Map<LocalDate, StudyPlanner> plannerMap = new HashMap<>();
        for (StudyPlanner p : existingPlanners) {
            plannerMap.put(p.getPlanDate(), p);
        }

        List<MenteeTodoBatchResponse.MenteeTodoItem> created = new ArrayList<>();
        LocalDate d = startDate;

        while (!d.isAfter(endDate)) {
            if (!selectedDays.isEmpty() && !selectedDays.contains(d.getDayOfWeek())) {
                d = d.plusDays(1);
                continue;
            }

            StudyPlanner planner = plannerMap.get(d);
            if (planner == null) {
                planner = studyPlannerRepository.save(StudyPlanner.builder()
                        .mentee(mentee)
                        .planDate(d)
                        .build());
                plannerMap.put(d, planner);
            }

            TodoTask saved = todoRepository.save(TodoTask.builder()
                    .planner(planner)
                    .worksheet(worksheetRef)
                    .content(title + " | " + goal)
                    .subject(subject)
                    .title(title)
                    .goal(goal)
                    .isCompleted(false)
                    .priority(1)
                    .taskType(TaskType.ASSIGNMENT)
                    .build());

            created.add(MenteeTodoBatchResponse.MenteeTodoItem.builder()
                    .taskId(saved.getId())
                    .date(d)
                    .subject(subject)
                    .goal(goal)
                    .title(title)
                    .status("UNDONE")
                    .createdBy(creatorRole)
                    .build());

            d = d.plusDays(1);
        }

        return MenteeTodoBatchResponse.builder()
                .menteeId(menteeUserId)
                .createdCount(created.size())
                .tasks(created)
                .build();
    }

    private Set<DayOfWeek> parseWeekdays(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) return Collections.emptySet();
        Set<DayOfWeek> result = new HashSet<>();
        for (String w : weekdays) {
            if (w == null) continue;
            String s = w.trim().toUpperCase();
            switch (s) {
                case "일": case "SUN": result.add(DayOfWeek.SUNDAY); break;
                case "월": case "MON": result.add(DayOfWeek.MONDAY); break;
                case "화": case "TUE": result.add(DayOfWeek.TUESDAY); break;
                case "수": case "WED": result.add(DayOfWeek.WEDNESDAY); break;
                case "목": case "THU": result.add(DayOfWeek.THURSDAY); break;
                case "금": case "FRI": result.add(DayOfWeek.FRIDAY); break;
                case "토": case "SAT": result.add(DayOfWeek.SATURDAY); break;
                default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요일 오류: " + w);
            }
        }
        return result;
    }
}