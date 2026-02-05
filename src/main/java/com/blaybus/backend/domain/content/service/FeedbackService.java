package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Feedback;
import com.blaybus.backend.domain.content.dto.request.FeedbackRequest;
import com.blaybus.backend.domain.content.dto.response.FeedbackResponse;
import com.blaybus.backend.domain.content.repository.FeedbackRepository;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.MentorProfileRepository;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TodoRepository todoRepository;

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Transactional
    public FeedbackResponse.Create createMentorFeedback(Long assignmentId, FeedbackRequest.Create request) {

        // 1) 로그인 유저 확인
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 피드백을 작성할 수 있습니다.");
        }

        // 2) 멘토 프로필 조회 (mentorId = userId)
        MentorProfile mentor = mentorProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멘토 프로필을 찾을 수 없습니다."));

        // 3) 과제(task) 조회
        TodoTask task = todoRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        // 4) 중복 작성 방지 (task당 1개)
        if (feedbackRepository.existsByTask_Id(assignmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 피드백이 작성된 과제입니다.");
        }

        // 5) 저장
        Feedback saved = feedbackRepository.save(
                Feedback.builder()
                        .task(task)
                        .mentor(mentor)
                        .content(request.content())
                        .build()
        );

        // 6) 응답 DTO
        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                mentor.getUserId(),
                user.getName(),
                user.getProfileImage()
        );

        return new FeedbackResponse.Create(
                saved.getId(),
                assignmentId,
                mentorInfo,
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public FeedbackResponse.Create getFeedback(Long assignmentId) {

        Feedback feedback = feedbackRepository.findByTask_Id(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백이 없습니다."));

        // mentorName/profileImage는 User에서 가져오기
        User mentorUser = feedback.getMentor().getUser();

        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                feedback.getMentor().getUserId(),
                mentorUser.getName(),
                mentorUser.getProfileImage()
        );

        return new FeedbackResponse.Create(
                feedback.getId(),
                assignmentId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt()
        );
    }

}
