package com.ticket.global.config;

import com.ticket.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/check-email",
                                "/api/users/check-phone",
                                "/api/auth/login"
                        ).permitAll()
                        // 공연, 일정, 좌석 조회는 비로그인도 가능
                        .requestMatchers("/api/concerts/**").permitAll()
                        .requestMatchers("/api/seats/schedule/**").permitAll()

                        // WebSocket 엔드포인트 허용 추가
                        .requestMatchers("/ws/**").permitAll()

                        // 테스트용 임시 허용 (테스트 후 삭제 예정)
                        //.requestMatchers("/api/seats/*/reserve").permitAll()

                        // 테스트용 API 허용 (테스트 후 삭제 예정)
                        //.requestMatchers("/api/test/**").permitAll()

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/*.html","/css/**", "/js/**").permitAll()

                        .anyRequest().authenticated()
                )

                // UsernamePasswordAuthenticationFilter(기본 로그인 처리)보다 '앞에서' 실행되도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}