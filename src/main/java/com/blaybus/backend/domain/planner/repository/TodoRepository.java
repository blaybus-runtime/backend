package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoTask, Long> {

    /**
     * 플래너에 속한 투두 전체 조회
     * 화면 출력/정렬 안정성 위해 priority -> id 순
     */
    List<TodoTask> findAllByPlanner_IdOrderByPriorityAscIdAsc(Long plannerId);

    // 특정 플래너에 속한 TodoTask를 피드백과 함께 LEFT JOIN 해서 가져오는 쿼리
    @Query("SELECT t FROM TodoTask t LEFT JOIN FETCH t.feedback WHERE t.planner.id = :plannerId")
    List<TodoTask> findAllByPlannerIdWithFeedback(@Param("plannerId") Long plannerId);
}
