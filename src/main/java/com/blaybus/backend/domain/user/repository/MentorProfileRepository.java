package com.blaybus.backend.domain.user.repository;

import com.blaybus.backend.domain.user.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
}
