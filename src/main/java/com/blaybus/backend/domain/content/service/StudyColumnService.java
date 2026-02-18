package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.dto.response.StudyColumnSummaryResponse;
import com.blaybus.backend.domain.content.repository.StudyColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyColumnService {

    private final StudyColumnRepository studyColumnRepository;

    public List<StudyColumnSummaryResponse> getRecentColumns(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        return studyColumnRepository.findAllByOrderByCreatedAtDesc(pageable).stream()
                .map(StudyColumnSummaryResponse::from)
                .collect(Collectors.toList());
    }
}
