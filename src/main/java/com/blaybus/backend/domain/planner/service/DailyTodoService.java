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


        // 1. SecurityUtils를 통해 현재 로그인한 유저의 username을 가져옴
        String username = SecurityUtils.getCurrentUsername();

        // 2. 해당 username으로 유저 엔티티 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        Long menteeId = user.getId(); // User의 ID를 사용
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 3. 기존 로직 수행 (menteeId 기반 조회)
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

    @Transactional
    public MenteeTodoBatchResponse createMenteeTodoBatch(MenteeTodoBatchRequest request) {

        // 1) 토큰에서 로그인 유저 확인
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        // 2) 멘티 권한 확인
        if (user.getRole() != Role.MENTEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘티 권한이 필요합니다.");
        }

        Long menteeId = user.getId();

        // 3) 멘티 요청 → 멘토 배치 요청으로 변환 (menteeId만 토큰 기반)
        MentorTodoBatchRequest converted = new MentorTodoBatchRequest();
        converted.setMenteeId(menteeId);
        converted.setSubject(request.getSubject());
        converted.setGoal(request.getGoal());
        converted.setTitle(request.getTitle());
        converted.setStartDate(request.getStartDate());
        converted.setEndDate(request.getEndDate());
        converted.setWeekdays(request.getWeekdays());
        converted.setWorksheetId(request.getWorksheetId());

        // 4) 기존 멘토 배치 생성 로직 재사용
        MentorTodoBatchResponse mentorResult = createMentorTodoBatch(converted);

        // 5) MentorTodoBatchResponse → MenteeTodoBatchResponse로 매핑
        // ⚠️ 아래 매핑은 MentorTodoBatchResponse 구조가 "menteeId/createdCount/tasks" 비슷하다는 전제.
        // 만약 필드명이 다르면, 그 클래스 코드 보여주면 내가 정확히 맞춰줄게.
        return MenteeTodoBatchResponse.builder()
                .menteeId(menteeId)
                .createdCount(mentorResult.getCreatedCount())
                .tasks(
                        mentorResult.getTasks().stream()
                                .map(t -> MenteeTodoBatchResponse.MenteeTodoItem.builder()
                                        .taskId(t.getTaskId())
                                        .date(t.getDate())
                                        .subject(t.getSubject())
                                        .goal(t.getGoal())
                                        .title(t.getTitle())
                                        .status(t.getStatus())
                                        .createdBy("MENTEE") // ✅ 멘티 생성이므로 강제
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Transactional // write 트랜잭션
    public MentorTodoBatchResponse createMentorTodoBatch( MentorTodoBatchRequest req) {

        String username = SecurityUtils.getCurrentUsername();
        User mentorUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (mentorUser.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 과제를 생성할 수 있습니다.");
        }

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
        }

        Long menteeId = req.getMenteeId();

        MenteeProfile mentee = em.find(MenteeProfile.class, menteeId);
        if (mentee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 멘티 프로필입니다.");
        }

        // ✅ worksheetId(optional) 처리: 있으면 참조 프록시 가져오기 (DB hit 최소화)
        Worksheet worksheetRef = null;
        if (req.getWorksheetId() != null) {
            worksheetRef = em.find(Worksheet.class, req.getWorksheetId());
            if (worksheetRef == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 worksheetId 입니다.");
            }
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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 요일 형식입니다: " + w);            }
        }
        return result;
    }
}
