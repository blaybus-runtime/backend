package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.Memo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    // 전체 조회(최신순)
    List<Memo> findByMentee_IdOrderByCreatedAtDesc(Long menteeId);

    // 홈카드 최신 limit개
    List<Memo> findByMentee_IdOrderByCreatedAtDesc(Long menteeId, Pageable pageable);
}
