package com.ticket.domain.concert.controller;

import com.ticket.domain.concert.dto.ScheduleDetailResponse;
import com.ticket.domain.concert.service.ConcertScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ConcertScheduleController {

    private final ConcertScheduleService scheduleService;

    // 일정 조회 (공연 정보 + 다른 일정 포함)
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getScheduleDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleDetail(scheduleId));
    }
}
