/*DailyStudyPlannerTodoRepository 는 스터디 플래너랑 TodoTask랑 함께 조회해야 하는 쿼리들만 모아놓는게 좋을 것 같아서
* 순수 스터디 플래너만 조희하는 용도로 레포지토리를 하나 더 만들었습니다
* -김동수-*/

package com.blaybus.backend.domain.planner.repository;

import com.blaybus.backend.domain.planner.StudyPlanner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlannerRepository extends JpaRepository<StudyPlanner, Long> {
}
