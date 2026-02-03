package com.blaybus.backend.domain.user.service;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.dto.LoginRequest;
import com.blaybus.backend.domain.user.dto.LoginResponse;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        System.out.println("RAW PW = [" + request.getPassword() + "]");
        System.out.println("ENCODER = " + passwordEncoder.getClass());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(LoginResponse.UserDto.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }

}
