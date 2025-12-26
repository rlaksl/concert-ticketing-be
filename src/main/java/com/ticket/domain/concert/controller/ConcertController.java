package com.ticket.domain.concert.controller;

import com.ticket.domain.concert.entity.Concert;
import com.ticket.domain.concert.entity.ConcertSchedule;
import com.ticket.domain.concert.service.ConcertScheduleService;
import com.ticket.domain.concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertScheduleService scheduleService;

    // 공연 전체 조회
    @GetMapping
    public ResponseEntity<List<Concert>> getAllConcerts() {
        return ResponseEntity.ok(concertService.getAllConcerts());
    }

    // 공연 단건 조회
    @GetMapping("/{concertId}")
    public ResponseEntity<Concert> getConcert(@PathVariable Long concertId) {
        return ResponseEntity.ok(concertService.getConcert(concertId));
    }

    // 특정 공연의 일정 목록 조회
    @GetMapping("/{concertId}/schedules")
    public ResponseEntity<List<ConcertSchedule>> getSchedules(@PathVariable Long concertId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByConcert(concertId));
    }
}