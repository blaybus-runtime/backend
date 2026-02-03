package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.repository.DailyTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyProgressService {
    private final DailyTodoRepository dailyTodoRepository;

    public
}
