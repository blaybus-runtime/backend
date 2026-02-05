package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.request.TaskStatusUpdateRequestDto;
import com.blaybus.backend.domain.planner.dto.response.TaskStatusUpdateResponseDto;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoStatusService {

    private final TodoRepository todoRepository;

    @Transactional
    public TaskStatusUpdateResponseDto updateTaskStatus(
            Long taskId,
            Long menteeId,
            TaskStatusUpdateRequestDto requestDto
    ) {
        // 1. task 조회
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 task입니다."));

        // 2. 소유권 검증 (task → planner → mentee → userId)
        Long ownerMenteeId = task.getPlanner()
                .getMentee()
                .getUser()
                .getId();

        if (!ownerMenteeId.equals(menteeId)) {
            throw new IllegalArgumentException("해당 task를 수정할 권한이 없습니다.");
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
