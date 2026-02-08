package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.notification.dto.event.NotificationEvent;
import com.blaybus.backend.domain.planner.*;
import com.blaybus.backend.domain.planner.dto.request.MenteeTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.request.MentorTodoBatchRequest;
import com.blaybus.backend.domain.planner.dto.response.*;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import com.blaybus.backend.domain.planner.repository.TimeRecordRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.NotificationType;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.enum_type.TaskType;
import com.blaybus.backend.global.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final TimeRecordRepository timeRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager em;

    /* =========================
       일일 조회
     ========================= */
    public DailyTodoResponseDto getDaily(LocalDate date) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        Long menteeId = user.getId();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return buildDailyResponse(menteeId, targetDate);
    }

    /**
     * 멘토가 특정 멘티의 일일 할 일 조회
     */
    public DailyTodoResponseDto getDailyForMentee(Long menteeId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return buildDailyResponse(menteeId, targetDate);
    }

    private DailyTodoResponseDto buildDailyResponse(Long menteeId, LocalDate targetDate) {
        Optional<StudyPlanner> plannerOpt =
                studyPlannerRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, targetDate);

        if (plannerOpt.isEmpty()) {
            return DailyTodoResponseDto.builder()
                    .menteeId(menteeId)
                    .date(targetDate)
                    .todos(Collections.emptyList())
                    .timeRecords(Collections.emptyList())
                    .build();
        }

        StudyPlanner planner = plannerOpt.get();
        List<TodoTask> tasks = todoRepository.findAllDailyByPlannerId(planner.getId());

        List<DailyTodoResponseDto.TodoDto> todoDtos = tasks.stream()
                .map(t -> {
                    // ✅ taskWorksheets -> worksheets DTO 변환
                    List<DailyTodoResponseDto.WorksheetDto> worksheetDtos =
                            (t.getTaskWorksheets() == null ? List.<DailyTodoResponseDto.WorksheetDto>of()
                                    : t.getTaskWorksheets().stream()
                                    .map(tw -> DailyTodoResponseDto.WorksheetDto.builder()
                                            .worksheetId(tw.getWorksheet().getId())
                                            .title(tw.getWorksheet().getTitle())
                                            .subject(tw.getWorksheet().getSubject())
                                            .fileUrl(tw.getWorksheet().getFileUrl())
                                            .weekdays(tw.getWeekdays())
                                            .build())
                                    .toList()
                            );

                    return DailyTodoResponseDto.TodoDto.builder()
                            .id(t.getId())
                            .content(t.getContent())
                            .subject(t.getSubject())
                            .isCompleted(t.isCompleted())
                            .priority(t.getPriority())
                            .taskType(t.getTaskType().name())
                            .title(t.getTitle())
                            .goal(t.getGoal())
                            .isFeedbackDone(t.getFeedback() != null && t.getFeedback().getContent() != null)
                            .worksheets(worksheetDtos)
                            .build();
                })
                .toList();

        List<TimeRecord> timeRecords = timeRecordRepository.findAllByPlanner_Id(planner.getId());
        List<DailyTodoResponseDto.TimeRecordDto> timeRecordDtos = timeRecords.stream()
                .map(r -> DailyTodoResponseDto.TimeRecordDto.builder()
                        .id(r.getId())
                        .subject(r.getSubject())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .toList();

        return DailyTodoResponseDto.builder()
                .menteeId(menteeId)
                .plannerId(planner.getId())
                .date(targetDate)
                .todos(todoDtos)
                .timeRecords(timeRecordDtos)
                .build();
    }

    /* =========================
       멘티 자가 생성 (기존 유지)
     ========================= */
    @Transactional
    public MenteeTodoBatchResponse createMenteeTodoBatch(MenteeTodoBatchRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MENTEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘티 권한이 필요합니다.");
        }

        // ✅ files가 null이어도 안전하게 빈 리스트 처리
        List<MentorTodoBatchRequest.FileItem> convertedFiles =
                Optional.ofNullable(request.getFiles())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(f -> {
                            MentorTodoBatchRequest.FileItem x = new MentorTodoBatchRequest.FileItem();
                            x.setWorksheetId(f.getWorksheetId());
                            x.setWeekdays(f.getWeekdays());
                            return x;
                        })
                        .toList();

        return generateTodoBatchV2(
                user.getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getWeekdays(),
                request.getSubject(),
                request.getTitle(),
                request.getGoal(),
                convertedFiles,
                "MENTEE"
        );
    }


    /* =========================
       멘토 생성 (다중 파일 지원)
     ========================= */
    @Transactional
    public MentorTodoBatchResponse createMentorTodoBatch(MentorTodoBatchRequest req) {
        String username = SecurityUtils.getCurrentUsername();
        User mentorUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (mentorUser.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 과제를 생성할 수 있습니다.");
        }

        MenteeTodoBatchResponse result = generateTodoBatchV2(
                req.getMenteeId(),
                req.getStartDate(),
                req.getEndDate(),
                req.getWeekdays(),      // task 생성 요일
                req.getSubject(),
                req.getTitle(),
                req.getGoal(),
                req.getFiles(),         // 파일별 설정
                "MENTOR"
        );

        // ==========================================
        // ▼ [추가] 알림 발송 로직 (생성된 건이 있을 때만)
        // ==========================================
        if (result.getCreatedCount() > 0) {
            // 알림 받을 멘티 정보 조회
            User menteeUser = userRepository.findById(req.getMenteeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멘티를 찾을 수 없습니다."));

            // 알림 클릭 시 이동할 URL (과제 시작일의 플래너로 이동)
            String redirectUrl = "/planner?date=" + req.getStartDate().toString();

            // 알림 메시지 생성 (예: "홍길동 멘토님이 [수학] 과제를 5개 등록했습니다.")
            String message = String.format("%s 멘토님이 [%s] 과제를 %d개 등록했습니다.",
                    mentorUser.getName(), req.getSubject(), result.getCreatedCount());

            // 이벤트 발행 -> NotificationService가 받아서 처리함
            eventPublisher.publishEvent(new NotificationEvent(
                    menteeUser,
                    message,
                    redirectUrl,
                    NotificationType.NEW_TODO
            ));
        }
        // ▲ [추가 끝]

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

    /* =========================
       기존 단일 worksheet 로직 (멘티용)
     ========================= */
    private MenteeTodoBatchResponse generateTodoBatch(
            Long menteeUserId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> weekdays,
            String subject,
            String title,
            String goal,
            Long worksheetId,
            String creatorRole
    ) {
        MenteeProfile mentee = em.find(MenteeProfile.class, menteeUserId);
        if (mentee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "멘티 프로필이 존재하지 않습니다.");
        }

        Worksheet worksheetRef = worksheetId != null ? em.find(Worksheet.class, worksheetId) : null;
        Set<DayOfWeek> selectedDays = parseWeekdays(weekdays);

        List<StudyPlanner> existingPlanners =
                studyPlannerRepository.findAllByMentee_UserIdAndPlanDateBetween(menteeUserId, startDate, endDate);

        Map<LocalDate, StudyPlanner> plannerMap = new HashMap<>();
        existingPlanners.forEach(p -> plannerMap.put(p.getPlanDate(), p));

        List<MenteeTodoBatchResponse.MenteeTodoItem> created = new ArrayList<>();
        LocalDate d = startDate;

        while (!d.isAfter(endDate)) {
            if (!selectedDays.isEmpty() && !selectedDays.contains(d.getDayOfWeek())) {
                d = d.plusDays(1);
                continue;
            }

            StudyPlanner planner = plannerMap.computeIfAbsent(d,
                    day -> studyPlannerRepository.save(
                            StudyPlanner.builder().mentee(mentee).planDate(day).build()
                    )
            );

            TodoTask saved = todoRepository.save(
                    TodoTask.builder()
                            .planner(planner)
                            .worksheet(worksheetRef)
                            .content(title + " | " + goal)
                            .subject(subject)
                            .title(title)
                            .goal(goal)
                            .isCompleted(false)
                            .priority(1)
                            .taskType(TaskType.ASSIGNMENT)
                            .build()
            );

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

    /* =========================
       멘토용 다중 파일 로직 (V2)
     ========================= */
    private MenteeTodoBatchResponse generateTodoBatchV2(
            Long menteeUserId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> taskWeekdays,
            String subject,
            String title,
            String goal,
            List<MentorTodoBatchRequest.FileItem> files,
            String creatorRole
    ) {
        if (files == null) files = Collections.emptyList();

        MenteeProfile mentee = em.find(MenteeProfile.class, menteeUserId);
        if (mentee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "멘티 프로필이 존재하지 않습니다.");
        }

        Set<DayOfWeek> selectedTaskDays = parseWeekdays(taskWeekdays);

        List<FileConfig> fileConfigs = new ArrayList<>();
        for (MentorTodoBatchRequest.FileItem f : files) {
            Worksheet ws = em.find(Worksheet.class, f.getWorksheetId());
            if (ws == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "worksheetId=" + f.getWorksheetId() + " 를 찾을 수 없습니다.");
            }

            List<String> fileWeekdays = (f.getWeekdays() == null || f.getWeekdays().isEmpty())
                    ? taskWeekdays
                    : f.getWeekdays();

            Set<DayOfWeek> days = parseWeekdays(fileWeekdays);
            fileConfigs.add(new FileConfig(ws, days, toWeekdayString(fileWeekdays)));
        }


        List<StudyPlanner> existingPlanners =
                studyPlannerRepository.findAllByMentee_UserIdAndPlanDateBetween(menteeUserId, startDate, endDate);

        Map<LocalDate, StudyPlanner> plannerMap = new HashMap<>();
        existingPlanners.forEach(p -> plannerMap.put(p.getPlanDate(), p));

        List<MenteeTodoBatchResponse.MenteeTodoItem> created = new ArrayList<>();
        LocalDate d = startDate;

        while (!d.isAfter(endDate)) {
            if (!selectedTaskDays.isEmpty() && !selectedTaskDays.contains(d.getDayOfWeek())) {
                d = d.plusDays(1);
                continue;
            }

            StudyPlanner planner = plannerMap.computeIfAbsent(d,
                    day -> studyPlannerRepository.save(
                            StudyPlanner.builder().mentee(mentee).planDate(day).build()
                    )
            );

            TodoTask task = todoRepository.save(
                    TodoTask.builder()
                            .planner(planner)
                            .worksheet(null) // 단일 FK 사용 안 함
                            .content(title + " | " + goal)
                            .subject(subject)
                            .title(title)
                            .goal(goal)
                            .isCompleted(false)
                            .priority(1)
                            .taskType(TaskType.ASSIGNMENT)
                            .build()
            );

            for (FileConfig fc : fileConfigs) {
                if (fc.days.isEmpty() || fc.days.contains(d.getDayOfWeek())) {
                    em.persist(
                            TaskWorksheet.builder()
                                    .task(task)
                                    .worksheet(fc.worksheet)
                                    .weekdays(fc.weekdays)
                                    .build()
                    );
                }
            }

            created.add(MenteeTodoBatchResponse.MenteeTodoItem.builder()
                    .taskId(task.getId())
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

    /* =========================
       유틸
     ========================= */
    private Set<DayOfWeek> parseWeekdays(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) return Collections.emptySet();

        Set<DayOfWeek> result = new HashSet<>();
        for (String w : weekdays) {
            if (w == null) continue;
            String s = w.trim().toUpperCase();
            switch (s) {
                case "일", "SUN" -> result.add(DayOfWeek.SUNDAY);
                case "월", "MON" -> result.add(DayOfWeek.MONDAY);
                case "화", "TUE" -> result.add(DayOfWeek.TUESDAY);
                case "수", "WED" -> result.add(DayOfWeek.WEDNESDAY);
                case "목", "THU" -> result.add(DayOfWeek.THURSDAY);
                case "금", "FRI" -> result.add(DayOfWeek.FRIDAY);
                case "토", "SAT" -> result.add(DayOfWeek.SATURDAY);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요일 오류: " + w);
            }
        }
        return result;
    }

    private String toWeekdayString(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) return null;
        return weekdays.stream()
                .filter(Objects::nonNull)
                .map(w -> w.trim().toUpperCase())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private static class FileConfig {
        Worksheet worksheet;
        Set<DayOfWeek> days;
        String weekdays;

        FileConfig(Worksheet worksheet, Set<DayOfWeek> days, String weekdays) {
            this.worksheet = worksheet;
            this.days = days;
            this.weekdays = weekdays;
        }
    }
}
