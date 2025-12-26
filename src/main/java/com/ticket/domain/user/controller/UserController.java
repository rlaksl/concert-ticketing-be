package com.ticket.domain.user.controller;

import com.ticket.domain.user.dto.SignUpRequest;
import com.ticket.domain.user.dto.SignUpResponse;
import com.ticket.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 API
    // 요청 주소: POST /api/users/signup
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // 전화번호 중복 확인
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneDuplicate(@RequestParam String phone) {
        boolean isDuplicate = userService.isPhoneDuplicate(phone);
        return ResponseEntity.ok(isDuplicate);
    }

    // 내 정보 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<String> getMyInfo() {
        return ResponseEntity.ok("인증된 사용자입니다!");
    }
}