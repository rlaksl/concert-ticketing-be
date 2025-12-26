package com.ticket.domain.seat.controller;

import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.service.SeatService;
import com.ticket.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // 특정 일정의 좌석 목록 조회
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Seat>> getSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(seatService.getSeatsBySchedule(scheduleId));
    }

    // 좌석 예약 (로그인 필요)
    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<Seat> reserveSeat(
            @PathVariable Long seatId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(seatService.reserveSeat(seatId, userDetails.getId()));
    }

    // 결제 완료 (로그인 필요)
    @PostMapping("/{seatId}/confirm")
    public ResponseEntity<String> confirmSeat(
            @PathVariable Long seatId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        seatService.confirmSeat(seatId, userDetails.getId());
        return ResponseEntity.ok("결제가 완료되었습니다.");
    }

    // 예약 취소 (로그인 필요)
    @PostMapping("/{seatId}/cancel")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Long seatId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        seatService.cancelReservation(seatId, userDetails.getId());
        return ResponseEntity.ok("예약이 취소되었습니다.");
    }
}