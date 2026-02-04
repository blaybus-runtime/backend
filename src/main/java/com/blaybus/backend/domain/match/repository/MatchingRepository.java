package com.blaybus.backend.domain.match.repository;

import com.blaybus.backend.domain.match.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    /*
    특정 멘토의 매칭 리스트를 조회하는 쿼리
    그냥 조회하면 N+1 쿼리 문제로 터져서 matching을 가져올 때 멘티프로필과 유저를 한 번에 join 해서 가져오는 것으로 구현했슨
     */
    @Query("SELECT m FROM Matching m " +
            "JOIN FETCH m.mentee mp " +
            "JOIN FETCH mp.user u " +
            "WHERE m.mentor.userId = :mentorId")
    List<Matching> findAllByMentorId(@Param("mentorId") Long mentorId);
}
