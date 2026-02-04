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

    @Transactional
    public MyInfoData updateMyInfoByUsername(String username, UpdateMyInfoRequest req) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1) User 공통 수정 (들어온 값만)
        if (req.getName() != null) user.updateName(req.getName());
        if (req.getProfileImage() != null) user.updateProfileImage(req.getProfileImage());

        String role = user.getRole().name();
        UpdateMyInfoRequest.Profile p = req.getProfile(); // profile 자체가 null일 수 있음

        if ("MENTEE".equals(role)) {
            MenteeProfile mp = menteeProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("MenteeProfile not found"));

            if (p != null) {
                if (p.getPhoneNumber() != null) mp.updatePhoneNumber(p.getPhoneNumber());
                if (p.getEmail() != null) mp.updateEmail(p.getEmail());
                if (p.getHighSchool() != null) mp.updateHighSchool(p.getHighSchool());
                if (p.getGrade() != null) mp.updateGrade(p.getGrade());
                if (p.getTargetUniv() != null) mp.updateTargetUniv(p.getTargetUniv());
                if (p.getMessageToMentor() != null) mp.updateMessageToMentor(p.getMessageToMentor());

                // subjects는 "있으면 교체", null이면 유지
                if (p.getSubjects() != null) {
                    mp.replaceSubjects(p.getSubjects());
                }
            }

        } else if ("MENTOR".equals(role)) {
            MentorProfile mp = mentorProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("MentorProfile not found"));

            if (p != null) {
                if (p.getMajor() != null) mp.updateMajor(p.getMajor());
                if (p.getStudentIdCard() != null) mp.updateStudentIdCard(p.getStudentIdCard());
                if (p.getBio() != null) mp.updateBio(p.getBio());
                if (p.getStatus() != null) mp.updateStatus(p.getStatus());
                if (p.getSubject() != null) mp.updateSubject(p.getSubject());
            }
        } else {
            throw new IllegalArgumentException("Unknown role: " + role);
        }

        // 수정 결과를 동일한 조회 DTO 형태로 리턴
        return getMyInfoByUsername(username);
    }


}
