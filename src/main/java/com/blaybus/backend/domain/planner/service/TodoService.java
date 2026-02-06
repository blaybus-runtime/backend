package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.TaskDetailResponse; // 추가된 import
import com.blaybus.backend.domain.planner.dto.response.TodoTaskSortedResponse;
import com.blaybus.backend.domain.planner.repository.StudyPlannerRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.global.exception.GeneralException; // 추가된 import
import com.blaybus.backend.global.response.status.ErrorStatus; // 추가된 import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final StudyPlannerRepository studyPlannerRepository;

    // [기존] 멘토용 할 일 정렬 조회
    public List<TodoTaskSortedResponse> getSortedTasksByMenteeAndDate(Long menteeId, LocalDate date) {

        StudyPlanner planner = studyPlannerRepository.findByMenteeIdAndPlanDate(menteeId, date)
                .orElse(null);

        if (planner == null) {
            return List.of();
        }

        List<TodoTask> tasks = todoRepository.findAllByPlannerIdWithFeedback(planner.getId());

        return tasks.stream()
                .sorted(Comparator.comparingInt(this::calculatePriority))
                .map(TodoTaskSortedResponse::from)
                .toList();
    }

    /**
     * [추가] 할 일 상세 조회 (학습지, 제출물, 피드백 포함)
     * - 엔티티 구조 변경(TaskWorksheet)에 대한 처리는 TaskDetailResponse.from() 내부에서 수행됩니다.
     */
    public TaskDetailResponse getTaskDetail(Long taskId) {
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._TASK_NOT_FOUND));

        return TaskDetailResponse.from(task);
    }

    // [기존] 우선순위 계산 헬퍼
    private int calculatePriority(TodoTask task) {
        boolean isTaskDone = task.isCompleted();
        boolean isFeedbackDone = (task.getFeedback() != null);

        if (isTaskDone && !isFeedbackDone) {
            return 1; // 1순위
        } else if (!isTaskDone && !isFeedbackDone) {
            return 2; // 2순위
        } else {
            return 3; // 3순위 (피드백 완료)
        }
    }
}