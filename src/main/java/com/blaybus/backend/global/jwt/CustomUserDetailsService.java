package com.blaybus.backend.global.jwt;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자"));
        return new CustomUserDetails(user);
    }
}
