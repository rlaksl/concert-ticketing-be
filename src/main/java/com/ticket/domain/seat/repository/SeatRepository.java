package com.ticket.domain.seat.repository;

import com.ticket.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 특정 일정의 모든 좌석 조회 (좌석번호 순)
    List<Seat> findByScheduleIdOrderBySeatNoAsc(Long scheduleId);
}
