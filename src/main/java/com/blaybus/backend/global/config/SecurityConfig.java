package com.blaybus.backend.global.config;

import com.blaybus.backend.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // ✅ 로그인은 열어두기
                        //.requestMatchers("/api/v1/mentor/**").permitAll()
                        //.requestMatchers("/api/v1/mentor/tasks/**").permitAll()
                        .requestMatchers("/api/v1/columns/**").permitAll() // ✅ 칼럼 조회는 열어두기
                        .requestMatchers("/api/v1/study/**").permitAll() // ✅ study 관련 기능도 열어두기
                        .requestMatchers("/api/v1/matchings/**").permitAll()
                        .requestMatchers("/api/v1/mentors/**").permitAll() // ✅ 멘티 생성 등
                        .requestMatchers("/api/v1/comments/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/mentees/**").hasRole("MENTEE")
                        .requestMatchers("/api/v1/mentor/**").hasAnyAuthority("MENTOR", "ROLE_MENTOR")
                        .requestMatchers("/api/v1/mentee/**").hasAnyAuthority("MENTEE", "ROLE_MENTEE")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://surl-study.pages.dev"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
