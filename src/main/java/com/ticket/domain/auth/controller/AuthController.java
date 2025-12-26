package com.ticket.domain.auth.controller;

import com.ticket.domain.auth.dto.LoginRequest;
import com.ticket.domain.auth.dto.TokenResponse;
import com.ticket.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 로그아웃 API (임시)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
