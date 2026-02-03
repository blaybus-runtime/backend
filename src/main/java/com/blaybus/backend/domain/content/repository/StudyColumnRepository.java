package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.StudyColumn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyColumnRepository extends JpaRepository<StudyColumn,Long> {

    List<StudyColumn> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
