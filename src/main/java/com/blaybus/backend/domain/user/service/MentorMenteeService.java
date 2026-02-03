package com.blaybus.backend.domain.user.service;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.dto.CreateMenteeRequest;
import com.blaybus.backend.domain.user.dto.CreateMenteeResponse;
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

    @Transactional
    public CreateMenteeResponse createMentee(Long mentorId, CreateMenteeRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
        }

        // 임시 비밀번호 생성
        String tempPassword = PasswordGenerator.generate(14);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(tempPassword);

        // 멘티 User 생성
        User mentee = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .role(Role.MENTEE)
                .build();

        userRepository.save(mentee);

        // 응답 (tempPassword는 응답으로만!)
        return CreateMenteeResponse.builder()
                .userId(mentee.getId())
                .username(mentee.getUsername())
                .name(mentee.getName())
                .role(mentee.getRole())
                .tempPassword(tempPassword)
                .build();
    }
}
