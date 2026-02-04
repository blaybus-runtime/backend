package com.blaybus.backend.domain.match.service;

import com.blaybus.backend.domain.match.Matching;
import com.blaybus.backend.domain.match.dto.response.MenteeCardResponse;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.planner.repository.DailyStudyPlannerTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

                    // 해당 날짜(targetDate)의 과제 완료 여부 체크
                    boolean isCompleted = checkDailyCompletion(menteeId, targetDate);

                    return MenteeCardResponse.builder()
                            .menteeId(menteeId)
                            .name(matching.getMentee().getUser().getName())
                            .profileImageUrl(matching.getMentee().getUser().getProfileImage())
                            .school(matching.getMentee().getSchoolName())
                            .isDailyTodoCompleted(isCompleted)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 헬퍼 메서드: 특정 멘티가 특정 날짜에 과제를 다 했는지 확인
    private boolean checkDailyCompletion(Long menteeId, LocalDate date) {
        return dailyStudyPlannerTodoRepository.findTop1ByMentee_UserIdAndPlanDateOrderByCreatedAtDesc(menteeId, date)
                .map(planner -> {
                    boolean hasTasks = !planner.getTasks().isEmpty();
                    boolean allFinished = planner.getTasks().stream().allMatch(task -> task.isCompleted());
                    return hasTasks && allFinished;
                })
                .orElse(false); // 플래너가 없으면 미완료(false) 처리
    }
}
