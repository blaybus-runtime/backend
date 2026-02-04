package com.blaybus.backend.domain.user.repository;

import com.blaybus.backend.domain.user.MenteeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenteeProfileRepository extends JpaRepository<MenteeProfile, Long> {
}