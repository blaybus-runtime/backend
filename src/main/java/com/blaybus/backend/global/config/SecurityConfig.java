package com.blaybus.backend.global.config;

import com.blaybus.backend.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // ✅ 로그인은 열어두기
                        .requestMatchers("/api/v1/mentor/**").permitAll()
                        .requestMatchers("/api/v1/mentor/tasks/**").permitAll()
                        .requestMatchers("/api/v1/study/**").permitAll()
                        .requestMatchers("/api/v1/columns/**").permitAll() // ✅ 칼럼 조회는 열어두기
                        .requestMatchers("/api/v1/study/**").permitAll() // ✅ study 관련 기능도 열어두기
                        .requestMatchers("/api/v1/matchings/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/mentors/**").hasRole("MENTOR")
                        .requestMatchers("/api/v1/mentees/**").hasRole("MENTEE")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
