package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {
    // TimeRecord에 겹치는 시간이 있는지 확인하는 쿼리
    @Query("SELECT COUNT(t) > 0 FROM TimeRecord t " +
            "WHERE t.planner.id = :plannerId " +
            "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    boolean existsOverlapTime(
            @Param("plannerId") Long plannerId,
            @Param("startTime")LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // 플래너 ID로 TimeRecord 목록 조회
    List<TimeRecord> findAllByPlanner_Id(Long plannerId);
}
