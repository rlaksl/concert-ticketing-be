package com.ticket.domain.concert.service;

import com.ticket.domain.concert.dto.ScheduleDetailResponse;
import com.ticket.domain.concert.entity.ConcertSchedule;
import com.ticket.domain.concert.repository.ConcertScheduleRepository;
import com.ticket.global.exception.CustomException;
import com.ticket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertScheduleService {

    private final ConcertScheduleRepository scheduleRepository;

    // 특정 공연의 일정 목록 조회
    public List<ConcertSchedule> getSchedulesByConcert(Long concertId) {
        return scheduleRepository.findByConcertIdOrderByConcertAtAsc(concertId);
    }

    // 일정 단건 조회
    public ConcertSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 일정 상세 조회 (공연 정보 + 다른 일정 포함)
    public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
        ConcertSchedule schedule = getSchedule(scheduleId);
        Long concertId = schedule.getConcert().getId();
        List<ConcertSchedule> allSchedules = getSchedulesByConcert(concertId);

        return ScheduleDetailResponse.of(schedule, allSchedules);
    }
}
