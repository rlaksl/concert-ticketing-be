package com.ticket.domain.concert.repository;

import com.ticket.domain.concert.entity.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    // 특정 공연의 모든 일정 조회 (날짜순 정렬)
    List<ConcertSchedule> findByConcertIdOrderByConcertAtAsc(Long concertId);
}
