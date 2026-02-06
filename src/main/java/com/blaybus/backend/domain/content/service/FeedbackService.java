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

    //피드백 작성
    @Transactional
    public FeedbackResponse.Create createMentorFeedback(Long taskId, FeedbackRequest.Create request) {

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 피드백을 작성할 수 있습니다.");
        }

        MentorProfile mentor = mentorProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멘토 프로필을 찾을 수 없습니다."));

        // 과제(task) 조회 (기존 assignmentId → taskId)
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        // 중복 작성 방지 (task당 1개)
        Feedback feedback = feedbackRepository.findByTask_Id(taskId).orElse(null);

        if (feedback == null) {
            feedback = feedbackRepository.save(
                    Feedback.builder()
                            .task(task)
                            .mentor(mentor)
                            .content(request.content())
                            .build()
            );
        } else {
            if (!feedback.getMentor().getUserId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 다시 작성할 수 있습니다.");
            }

            if (feedback.getContent() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 피드백이 작성된 과제입니다.");
            }
            feedback.updateContent(request.content());
        }

        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                mentor.getUserId(),
                user.getName(),
                user.getProfileImage()
        );

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt()
        );
    }

    //피드백 조회
    @Transactional(readOnly = true)
    public FeedbackResponse.Create getFeedback(Long taskId) {

        Feedback feedback = feedbackRepository.findByTask_Id(taskId).orElse(null);
        if (feedback == null || feedback.getContent() == null) {
            return null;
        }

        User mentorUser = feedback.getMentor().getUser();

        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                feedback.getMentor().getUserId(),
                mentorUser.getName(),
                mentorUser.getProfileImage()
        );

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt()
        );
    }

    //피드백 수정
    @Transactional
    public FeedbackResponse.Create updateMentorFeedback(
            Long taskId,
            FeedbackRequest.Create request
    ) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."
                ));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "멘토만 피드백을 수정할 수 있습니다."
            );
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수정할 피드백이 없습니다."
                ));

        if (feedback.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 피드백이 없습니다.");
        }

        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 수정할 수 있습니다."
            );
        }

        feedback.updateContent(request.content());

        User mentorUser = feedback.getMentor().getUser();
        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                mentorUser.getId(),
                mentorUser.getName(),
                mentorUser.getProfileImage()
        );

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt()
        );
    }

    //피드백 삭제
    @Transactional
    public void deleteMentorFeedback(Long taskId) {

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."
                ));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "멘토만 피드백을 삭제할 수 있습니다."
            );
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "삭제할 피드백이 없습니다."
                ));

        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 삭제할 수 있습니다."
            );
        }

        feedback.clearContent();
    }



}
