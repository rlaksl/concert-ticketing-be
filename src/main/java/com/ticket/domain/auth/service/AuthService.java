package com.ticket.domain.auth.service;

import com.ticket.domain.auth.dto.LoginRequest;
import com.ticket.domain.auth.dto.TokenResponse;
import com.ticket.domain.user.entity.User;
import com.ticket.domain.user.repository.UserRepository;
import com.ticket.global.exception.CustomException;
import com.ticket.global.exception.ErrorCode;
import com.ticket.global.security.JwtTokenProvider;
import com.ticket.global.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    // 로그인
    @Transactional
    public TokenResponse login (LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 레디스에 저장
        tokenService.saveAccessToken(user.getId(), accessToken);
        tokenService.saveRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        tokenService.deleteTokens(userId);
    }
}
