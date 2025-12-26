package com.ticket.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에서 토큰 꺼내기
        String token = resolveToken(request);

        // 토큰이 있고 서명이 정상적인지 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {

            Long userId = jwtTokenProvider.getUserId(token);

            // 레디스에 있는 최신 토큰과 일치하는지 확인 (중복 로그인 방지)
            if (!tokenService.isValidAccessToken(userId, token)) {
                log.warn("중복 로그인 감지! userId: {}", userId);

                // 401 에러를 명확하게 보냄
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"다른 기기에서 로그인되어 로그아웃되었습니다.\"}");
                return; // 여기서 요청 종료 (컨트롤러로 안 넘어감)
            }

            // 모든 검사를 통과했으면 인증 객체 생성(DB에서 회원 정보를 조회해서 SecurityContext에 넣어줌)
            UserDetails userDetails = userDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.info("인증 성공: userId={}", userId);
        }

        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer {토큰} 형태에서 토큰만 잘라내는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
