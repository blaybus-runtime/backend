package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.request.TaskStatusUpdateRequestDto;
import com.blaybus.backend.domain.planner.dto.response.TaskStatusUpdateResponseDto;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TodoStatusService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskStatusUpdateResponseDto updateTaskStatus(
            Long taskId,
            TaskStatusUpdateRequestDto requestDto
    ) {
        //현재 로그인 유저 확인 로직
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        // 1. task 조회
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 task입니다."));

        // 2. 소유권 검증 (task → planner → mentee → userId)
        Long ownerUserId = task.getPlanner()
                .getMentee()
                .getUserId();

        if (!ownerUserId.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 task를 수정할 권한이 없습니다.");
        }

        // 3. 완료 상태 변경
        task.updateCompleted(requestDto.getIsCompleted());

        // 4. 응답 DTO 반환
        return new TaskStatusUpdateResponseDto(
                task.getId(),
                task.isCompleted()
        );
    }
}
