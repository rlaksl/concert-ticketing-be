package com.ticket.global.security;

import com.ticket.global.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    // 레디스에 저장할 때 사용할 키 접두사
    private static final String ACCESS_TOKEN_PREFIX = "access:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    // Access Token 저장
    public void saveAccessToken(Long userId, String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                accessToken,
                jwtProperties.getAccessTokenExpiration(), // 만료시간
                TimeUnit.MILLISECONDS // 시간단위
        );
    }

    // Refresh Token 저장
    public void saveRefreshToken (Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS

        );
    }

    // Access Token 조회
    public String getAccessToken(Long userId) {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
    }

    // Refresh Token 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // 검증
    public boolean isValidAccessToken(Long userId, String accessToken) {
        String savedToken = getAccessToken(userId);
        return accessToken.equals(savedToken);
    }

    // 삭제
    public void deleteTokens(Long userId) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }



}

