package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.TodoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoTask, Long> {

    List<TodoTask> findAllByPlanner_IdOrderByPriorityAscIdAsc(Long plannerId);

    @Query("SELECT t FROM TodoTask t LEFT JOIN FETCH t.feedback WHERE t.planner.id = :plannerId")
    List<TodoTask> findAllByPlannerIdWithFeedback(@Param("plannerId") Long plannerId);

    // ✅ 추가: Daily 조회용 (taskWorksheets + worksheet를 한번에 가져오기)
    @Query("""
        SELECT DISTINCT t
        FROM TodoTask t
        LEFT JOIN FETCH t.feedback f
        LEFT JOIN FETCH t.taskWorksheets tw
        LEFT JOIN FETCH tw.worksheet w
        WHERE t.planner.id = :plannerId
        ORDER BY t.priority ASC, t.id ASC
    """)
    List<TodoTask> findAllDailyByPlannerId(@Param("plannerId") Long plannerId);
}
