package com.blaybus.backend.domain.user.repository;

import com.blaybus.backend.domain.user.MenteeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface MenteeProfileRepository extends JpaRepository<MenteeProfile, Long> {

    @EntityGraph(attributePaths = "subjects")
    Optional<MenteeProfile> findWithSubjectsByUserId(Long userId);
}