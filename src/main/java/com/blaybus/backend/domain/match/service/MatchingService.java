package com.blaybus.backend.domain.match.service;

import com.blaybus.backend.domain.match.Matching;
import com.blaybus.backend.domain.match.dto.response.MenteeCardResponse;
import com.blaybus.backend.domain.match.dto.response.MenteeTaskResponse;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchingRepository matchingRepository;
    private final DailyStudyPlannerTodoRepository dailyStudyPlannerTodoRepository;

    public List<MenteeCardResponse> getMyMentees(Long mentorId, LocalDate targetDate) {

        // 멘토와 매칭된 모든 멘티 목록 조회
        List<Matching> matchings = matchingRepository.findAllByMentorId(mentorId);

        // 각 멘티별로 순회하며 DTO 생성
        return matchings.stream()
                .map(matching -> {
                    Long menteeId = matching.getMentee().getUserId();

                    // 해당 날짜의 미완료 피드백 개수 계산
                    int feedbackCount = countUnwrittenFeedbacks(menteeId, targetDate);

                    return MenteeCardResponse.builder()
                            .menteeId(menteeId)
                            .name(matching.getMentee().getUser().getName())
                            .profileImageUrl(matching.getMentee().getUser().getProfileImage())
                            .unwrittenFeedbackCount(feedbackCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MenteeTaskResponse> getMenteeDailyTasks(Long mentorId, LocalDate targetDate) {
        // 현재 멘토의 매칭 정보를 모두 가져옴
        List<Matching> matchings = matchingRepository.findAllByMentorId(mentorId);
        List<MenteeTaskResponse> allTasks = new ArrayList<>();

        for (Matching matching : matchings) {
            Long menteeId = matching.getMentee().getUserId();
            String menteeName = matching.getMentee().getUser().getName();

            //해당 날짜의 플래너를 조회
            dailyStudyPlannerTodoRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, targetDate)
                    .ifPresent(planner -> {
                        planner.getTasks().forEach(task -> {
                            allTasks.add(MenteeTaskResponse.builder()
                                    .menteeId(menteeId)
                                    .menteeName(menteeName)
                                    .plannerId(planner.getId())
                                    .taskId(task.getId())
                                    .taskContent(task.getContent())
                                    .isCompleted(task.isCompleted())
                                    .build());
                        });
                    });
        }

        return allTasks;
    }

    // 헬퍼 메서드: 특정 멘티가 특정 날짜에 과제를 다 했는지 확인
    private int countUnwrittenFeedbacks(Long menteeId, LocalDate date) {
        return dailyStudyPlannerTodoRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, date)
                .map(planner -> (int) planner.getTasks().stream()
                        .filter(task -> task.isCompleted() && task.getFeedback() == null)
                        .count())
                .orElse(0); // 플래너가 없으면 카운트 0
    }
}
