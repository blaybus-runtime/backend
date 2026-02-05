package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Comment;
import com.blaybus.backend.domain.content.dto.request.CommentRequest;
import com.blaybus.backend.domain.content.dto.response.CommentResponse;
import com.blaybus.backend.domain.content.repository.CommentRepository;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    // 댓글 작성 서비스
    @Transactional
    public void createComment(Long userId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        TodoTask todoTask = todoRepository.findById(request.taskId())
                .orElseThrow(() -> new RuntimeException("Todo를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.content())
                .user(user)
                .todoTask(todoTask)
                .build();

        commentRepository.save(comment);
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
}
