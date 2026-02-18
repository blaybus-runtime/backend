package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Comment;
import com.blaybus.backend.domain.content.dto.request.CommentRequest;
import com.blaybus.backend.domain.content.dto.request.CommentUpdateRequest;
import com.blaybus.backend.domain.content.dto.response.CommentResponse;
import com.blaybus.backend.domain.content.repository.CommentRepository;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.notification.dto.event.NotificationEvent;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.NotificationType;
import com.blaybus.backend.global.exception.GeneralException;
import com.blaybus.backend.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final MatchingRepository matchingRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 댓글 작성 서비스
    @Transactional
    public void createComment(Long userId, CommentRequest request) {
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        TodoTask todoTask = todoRepository.findById(request.taskId())
                .orElseThrow(() -> new RuntimeException("Todo를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.content())
                .user(writer)
                .todoTask(todoTask)
                .build();

        commentRepository.save(comment);

        // ==========================================
        // [추가] 3. 알림 이벤트 발행 (핵심 로직)
        // ==========================================

        // 알림 받을 사람 결정 (헬퍼 메서드 사용)
        User receiver = determineReceiver(todoTask, writer);

        // 수신자가 존재하고, 본인이 아닐 경우에만 알림 발송
        if (receiver != null && !receiver.getId().equals(writer.getId())) {
            eventPublisher.publishEvent(new NotificationEvent(
                    receiver,                                      // 받는 사람
                    writer.getName() + "님이 댓글을 남겼습니다.",       // 알림 내용
                    "/tasks/" + todoTask.getId(),                  // 이동할 URL
                    NotificationType.NEW_COMMENT                   // 알림 타입
            ));
        }
    }

    // [내부 메서드] 알림 수신자 결정 로직
    private User determineReceiver(TodoTask task, User writer) {
        // 이 Task의 주인(멘티) 유저를 가져옵니다.
        User taskOwnerMentee = task.getPlanner().getMentee().getUser();
        Long menteeUserId = taskOwnerMentee.getId();

        // 1. 댓글 작성자가 '멘티(학생)'인 경우 -> '멘토'에게 알림
        if (writer.getId().equals(menteeUserId)) {
            // 매칭 테이블에서 담당 멘토 조회
            return matchingRepository.findAllByMenteeId(menteeUserId).stream()
                    .findFirst()
                    .map(matching -> matching.getMentor().getUser())
                    .orElse(null);
        }

        // 2. 댓글 작성자가 '멘토'인 경우 (혹은 제3자) -> '멘티'에게 알림
        return taskOwnerMentee;
    }

    public List<CommentResponse> getComments(Long taskId, Long currentUserId) {
        if (!todoRepository.existsById(taskId)) {
            throw new RuntimeException("todo를 찾을 수 없음");
        }

        List<Comment> comments = commentRepository.findByTaskId(taskId);

        return comments.stream()
                .map(comment -> CommentResponse.from(comment, currentUserId))
                .collect(Collectors.toList());
    }

    // [추가] 댓글 수정
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));

        // 작성자 검증
        validateWriter(userId, comment);

        // 내용 변경 (Dirty Checking으로 자동 저장)
        comment.updateContent(request.content());
    }

    // [추가] 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));

        // 작성자 검증
        validateWriter(userId, comment);

        commentRepository.delete(comment);
    }

    // [내부 메서드] 작성자 본인 확인 로직
    private void validateWriter(Long userId, Comment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._COMMENT_NOT_WRITER);
        }
    }
}
