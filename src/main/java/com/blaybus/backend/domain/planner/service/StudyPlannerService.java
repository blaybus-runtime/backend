/*
* 정은이가 이미 DailyTodoService 만들어서 스터디 플래너 엔티티랑 관련된 서비스들을 좀 짠 것 같긴 한데
* 뭔가 진짜 매일 할 일(DailyTodo)랑 관련있는 서비스만 몰아놓는게 좋을 것 같아서, TodoTask랑 관련 없지만 플래너 서비스에 들어가는
* 기능들 모아놓으려고 새로 하나 만들었습니당
* - 김동수 -
* */
package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TimeRecord;
import com.blaybus.backend.domain.planner.dto.request.TimeRecordRequest;
import com.blaybus.backend.domain.planner.repository.StudyPlannerRepository;
import com.blaybus.backend.domain.planner.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyPlannerService {

    private final TimeRecordRepository timeRecordRepository;
    private final StudyPlannerRepository studyPlannerRepository;

    public Long recordStudyTime(Long menteeId, TimeRecordRequest request) {
        StudyPlanner planner = studyPlannerRepository.findById(request.plannerId())
                .orElseThrow(() -> new RuntimeException("플래너가 존재하지 않습니다."));

        if(!planner.getMentee().getUserId().equals(menteeId)) {
            throw new RuntimeException("자신의 플래너만 조회할 수 있습니다.");
        }

        if(!request.startTime().isBefore(request.endTime())) {
            throw new RuntimeException("시작시간은 종료시간보다 늦을 수 없습니다.");
        }

        boolean isOverlapped = timeRecordRepository.existsOverlapTime(
                planner.getId(), request.startTime(), request.endTime()
        );

        if(isOverlapped) {
            throw new RuntimeException("헤당 시간대에 이미 공부 기록이 있습니다.");
        }

        TimeRecord timeRecord = TimeRecord.builder()
                .planner(planner)
                .subject(request.subject())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        timeRecordRepository.save(timeRecord);

        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        planner.addStudyTime((int) minutes);

        return timeRecord.getId();
    }
}
