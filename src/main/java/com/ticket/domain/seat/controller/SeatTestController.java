package com.ticket.domain.seat.controller;

import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 테스트 전용 Controller /JMeter 부하 테스트용 (인증 없이 접근 가능)
@RestController
@RequestMapping("/api/test/seats")
@RequiredArgsConstructor
public class SeatTestController {

    private final SeatService seatService;

    // 좌석 예약 테스트 (userId를 파라미터로 받음)
    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<Seat> reserveSeatForTest(
            @PathVariable Long seatId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(seatService.reserveSeat(seatId, userId));
    }
}