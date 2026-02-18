package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.TaskWorksheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskWorksheetRepository extends JpaRepository<TaskWorksheet, Long> {
    List<TaskWorksheet> findAllByTask_Id(Long taskId);
}
