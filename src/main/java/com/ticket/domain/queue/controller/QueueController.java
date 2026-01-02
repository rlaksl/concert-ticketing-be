package com.ticket.domain.queue.controller;

import com.ticket.domain.queue.dto.QueueEnterRequest;
import com.ticket.domain.queue.dto.QueueStatusResponse;
import com.ticket.domain.queue.service.QueueService;
import com.ticket.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {
    private final QueueService queueService;

    // 대기열 등록 API - 예매하기 버튼 클릭 시 호출
    @PostMapping("/enter")
    public ResponseEntity<QueueStatusResponse> enterQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QueueEnterRequest request) {

        QueueStatusResponse response = queueService.enterQueue(request.scheduleId(), userDetails.getId());
        return ResponseEntity.ok(response);
    }

    // 내 대기 상태 조회 API (Polling) - 주기적으로 호출하여 현재 순번과 상태를 확인
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long scheduleId) {

        QueueStatusResponse response = queueService.getQueueStatus(scheduleId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    // 대기열 이탈 API - 대기 취소 버튼 클릭 또는 브라우저 종료 시 호출
    @PostMapping("/exit")
    public ResponseEntity<String> exitQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QueueEnterRequest request) {

        queueService.exitQueue(request.scheduleId(), userDetails.getId());
        return ResponseEntity.ok("대기열에서 이탈하였습니다.");
    }

    // 입장 토큰 검증 API - 좌석 페이지 진입 전 또는 진입 직후 유효성 체크
    @GetMapping("/token/validate")
    public ResponseEntity<Boolean> validateToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long scheduleId,
            @RequestParam String token) {

        boolean isValid = queueService.validateEntryToken(scheduleId, userDetails.getId(), token);
        return ResponseEntity.ok(isValid);
    }
}
