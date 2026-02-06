package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.TodoTaskSortedResponse;
import com.blaybus.backend.domain.planner.repository.StudyPlannerRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
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

    // 피드백 유무를 기준으로 TodoTask 간의 정렬 순위를 계산하는 헬퍼 함수
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
