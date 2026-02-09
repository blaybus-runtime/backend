package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByTask_IdAndMenteeId(Long taskId, Long menteeId);

    // ✅ [추가] 멘티 ID와 Task ID 목록을 받아, 실제로 제출 내역이 존재하는 Task ID들만 반환
    @Query("SELECT s.task.id FROM Submission s WHERE s.menteeId = :menteeId AND s.task.id IN :taskIds")
    Set<Long> findTaskIdsByMenteeIdAndTaskIds(@Param("menteeId") Long menteeId, @Param("taskIds") List<Long> taskIds);
}
