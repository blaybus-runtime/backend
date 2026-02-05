package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.content.Worksheet; // ⚠️ 실제 Worksheet 엔티티 패키지로 맞추기
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorksheetRepository extends JpaRepository<Worksheet, Long> {
}
