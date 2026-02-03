package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoTask, Long> {

    /**
     * 플래너에 속한 투두 전체 조회
     * 화면 출력/정렬 안정성 위해 priority -> id 순
     */
    List<TodoTask> findAllByPlanner_IdOrderByPriorityAscIdAsc(Long plannerId);
}
