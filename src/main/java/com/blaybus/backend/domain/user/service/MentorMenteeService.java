package com.blaybus.backend.domain.user.service;

import com.blaybus.backend.domain.match.Matching;
import com.blaybus.backend.domain.match.repository.MatchingRepository;
import com.blaybus.backend.domain.user.MenteeProfile;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.dto.CreateMenteeRequest;
import com.blaybus.backend.domain.user.dto.CreateMenteeResponse;
import com.blaybus.backend.domain.user.repository.MenteeProfileRepository;
import com.blaybus.backend.domain.user.repository.MentorProfileRepository;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.util.PasswordGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentorMenteeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenteeProfileRepository menteeProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MatchingRepository matchingRepository;

    @Transactional
    public CreateMenteeResponse createMentee(Long mentorId, CreateMenteeRequest request) {

        // 1) username 자동 생성
        String username = generateUniqueMenteeUsername();

        // 2) 임시 비밀번호 생성 + 암호화
        String tempPassword = PasswordGenerator.generate(14);
        String encodedPassword = passwordEncoder.encode(tempPassword);

        // 3) User(MENTEE) 생성/저장
        User mentee = User.builder()
                .username(username)
                .password(encodedPassword)
                .name(request.getName())
                .role(Role.MENTEE)
                .profileImage(null)
                .build();

        userRepository.save(mentee);

        // 4) MenteeProfile 생성/저장
        CreateMenteeRequest.MenteeProfileDto p = request.getMenteeProfile();
        if (p == null) {
            throw new IllegalArgumentException("menteeProfile은 필수입니다.");
        }

        MenteeProfile menteeProfile = MenteeProfile.builder()
                .user(mentee)
                .phoneNumber(p.getPhoneNumber())
                .email(p.getEmail())
                .highSchool(p.getHighSchool())
                .grade(p.getGrade())
                .targetUniv(p.getTargetUniv())
                .subjects(p.getSubjects())
                .messageToMentor(p.getMessageToMentor())
                .build();

        menteeProfileRepository.save(menteeProfile);

        // 5) 멘토-멘티 매칭 생성
        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 멘토 ID입니다."));

        Matching matching = Matching.builder()
                .mentor(mentorProfile)
                .mentee(menteeProfile)
                .subject(p.getSubjects() != null && !p.getSubjects().isEmpty()
                        ? p.getSubjects().get(0) : "미정")
                .build();
        matchingRepository.save(matching);

        // 6) 응답 DTO 구성
        return CreateMenteeResponse.builder()
                .user(CreateMenteeResponse.UserDto.builder()
                        .userId(mentee.getId())
                        .username(mentee.getUsername())
                        .name(mentee.getName())
                        .role(mentee.getRole())
                        .profileImage(mentee.getProfileImage())
                        .build())
                .tempPassword(tempPassword)
                .menteeProfile(CreateMenteeResponse.MenteeProfileDto.builder()
                        .phoneNumber(menteeProfile.getPhoneNumber())
                        .email(menteeProfile.getEmail())
                        .highSchool(menteeProfile.getHighSchool())
                        .grade(menteeProfile.getGrade())
                        .targetUniv(menteeProfile.getTargetUniv())
                        .subjects(menteeProfile.getSubjects())
                        .messageToMentor(menteeProfile.getMessageToMentor())
                        .build())
                .build();
    }

    private String generateUniqueMenteeUsername() {
        int seq = 1;
        while (true) {
            String candidate = "mentee" + String.format("%02d", seq); // mentee01, mentee02...
            if (!userRepository.existsByUsername(candidate)) {
                return candidate;
            }
            seq++;
            if (seq > 9999) {
                throw new IllegalStateException("멘티 username 생성에 실패했습니다.");
            }
        }
    }
}
