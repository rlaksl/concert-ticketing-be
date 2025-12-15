package com.ticket.domain.user.service;

import com.ticket.domain.user.dto.SignUpRequest;
import com.ticket.domain.user.dto.SignUpResponse;
import com.ticket.domain.user.entity.User;
import com.ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 전화번호 중복 체크
        if (userRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // user 엔티티 생성
        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .phone(request.phone())
                .build();

        // DB 저장
        User savedUser = userRepository.save(user);

        // 응답 반환
        return SignUpResponse.from(savedUser);
    }
}
