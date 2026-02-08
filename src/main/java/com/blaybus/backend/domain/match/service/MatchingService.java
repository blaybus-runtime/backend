package com.blaybus.backend.domain.match.service;

import com.blaybus.backend.domain.match.Matching;
import com.blaybus.backend.domain.match.dto.response.MenteeCardResponse;
import com.blaybus.backend.domain.match.dto.response.MenteeFeedbackResponse;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.planner.Submission;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchingRepository matchingRepository;
    private final DailyStudyPlannerTodoRepository dailyStudyPlannerTodoRepository;

    // 멘토 홈 화면 - 좌측 멘티 카드 조회 api
    public List<MenteeCardResponse> getMyMentees(Long mentorId, LocalDate targetDate) {

        // 멘토와 매칭된 모든 멘티 목록 조회
        List<Matching> matchings = matchingRepository.findAllByMentorId(mentorId);

        // 각 멘티별로 순회하며 DTO 생성
        return matchings.stream()
                .map(matching -> {
                    Long menteeId = matching.getMentee().getUserId();

                    // 해당 날짜의 미완료 피드백 개수 계산
                    int feedbackCount = countUnwrittenFeedbacks(menteeId, targetDate);

                    // @ElementCollection(LAZY) subjects를 트랜잭션 안에서 초기화
                    List<String> subjects = new ArrayList<>(matching.getMentee().getSubjects());

                    return MenteeCardResponse.builder()
                            .menteeId(menteeId)
                            .name(matching.getMentee().getUser().getName())
                            .profileImageUrl(matching.getMentee().getUser().getProfileImage())
                            .unwrittenFeedbackCount(feedbackCount)
                            .highSchool(matching.getMentee().getHighSchool())
                            .grade(matching.getMentee().getGrade())
                            .subjects(subjects)
                            .targetUniv(matching.getMentee().getTargetUniv())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 멘토 홈 우측 화면 - 미완료 피드백(Pending Feedbacks) 목록 조회 api
    public List<MenteeFeedbackResponse> getPendingFeedbacks(Long mentorId, LocalDate targetDate) {

        List<Matching> matchings = matchingRepository.findAllByMentorId(mentorId); // 일단 멘토 id에 해당하는 매칭을 모두 가져옴
        List<MenteeFeedbackResponse> pendingList = new ArrayList<>(); // pending 상태인 과제를 넣을 리스트

        for (Matching matching : matchings) {
            Long menteeId = matching.getMentee().getUserId();
            String menteeName = matching.getMentee().getUser().getName();

            // 해당 날짜의 플래너 조회
            dailyStudyPlannerTodoRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, targetDate)
                    .ifPresent(planner -> {
                        planner.getTasks().stream()
                                // 멘토 피드백이 없는 할일 (과제 완료 여부 관계없이)
                                .filter(task -> task.getFeedback() == null)
                                .forEach(task -> {

                                    // ✅ 수정된 시간 결정 로직 (1:N 대응)
                                    LocalDateTime completedTime;

                                    // 제출물 리스트가 존재하고 비어있지 않다면
                                    if (task.getSubmissions() != null && !task.getSubmissions().isEmpty()) {
                                        // 여러 장의 사진 중 가장 '최근'에 업로드한 시간 선택
                                        completedTime = task.getSubmissions().stream()
                                                .map(Submission::getCreatedAt) // 생성 시간 추출
                                                .max(Comparator.naturalOrder()) // 최신순 비교
                                                .orElse(planner.getCreatedAt()); // (List가 비었을 때 대비 Fallback)
                                    } else {
                                        // 제출물이 없는 경우(단순 완료 체크), 플래너 생성 시간으로 대체
                                        completedTime = planner.getCreatedAt();
                                    }

                                    pendingList.add(MenteeFeedbackResponse.builder()
                                            .menteeId(menteeId)
                                            .menteeName(menteeName)
                                            .plannerId(planner.getId())
                                            .taskId(task.getId())
                                            .subject(task.getSubject())
                                            .taskContent(task.getContent())
                                            .completedAt(completedTime) // ✅ 계산된 시간 주입
                                            .build());
                                });
                    });
        }

        return pendingList;
    }

    // 헬퍼 메서드: 특정 멘티가 특정 날짜에 피드백이 없는 할일 수 계산
    private int countUnwrittenFeedbacks(Long menteeId, LocalDate date) {
        return dailyStudyPlannerTodoRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, date)
                .map(planner -> (int) planner.getTasks().stream()
                        .filter(task -> task.getFeedback() == null)
                        .count())
                .orElse(0); // 플래너가 없으면 카운트 0
    }
}
