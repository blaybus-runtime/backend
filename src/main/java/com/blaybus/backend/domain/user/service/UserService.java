package com.blaybus.backend.domain.user.service;

import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.dto.*;
import com.blaybus.backend.domain.user.repository.MenteeProfileRepository;
import com.blaybus.backend.domain.user.repository.MentorProfileRepository;
import com.blaybus.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MenteeProfileRepository menteeProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public MyInfoData getMyInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Object profile;

        // Role 타입이 enum이면 user.getRole().name() 형태로 비교
        String role = user.getRole().name();

        if ("MENTEE".equals(role)) {
            MenteeProfile mp = menteeProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("MenteeProfile not found"));

            profile = MenteeProfileDto.builder()
                    .type("MENTEE")
                    .phoneNumber(mp.getPhoneNumber())
                    .email(mp.getEmail())
                    .highSchool(mp.getHighSchool())
                    .grade(mp.getGrade())
                    .subjects(new java.util.ArrayList<>(mp.getSubjects()))
                    .targetUniv(mp.getTargetUniv())
                    .messageToMentor(mp.getMessageToMentor())
                    .build();

        } else if ("MENTOR".equals(role)) {
            MentorProfile mp = mentorProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("MentorProfile not found"));

            profile = MentorProfileDto.builder()
                    .type("MENTOR")
                    .major(mp.getMajor())
                    .studentIdCard(mp.getStudentIdCard())
                    .bio(mp.getBio())
                    .status(mp.isStatus())
                    .subject(mp.getSubject())
                    .build();

        } else {
            throw new IllegalArgumentException("Unknown role: " + role);
        }

        return MyInfoData.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(role)
                .profileImage(user.getProfileImage())
                .profile(profile)
                .build();
    }
}
