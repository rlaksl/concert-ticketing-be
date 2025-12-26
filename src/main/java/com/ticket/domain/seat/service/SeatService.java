package com.ticket.domain.seat.service;

import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.entity.SeatStatus;
import com.ticket.domain.seat.repository.SeatRepository;
import com.ticket.global.exception.CustomException;
import com.ticket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;

    // 특정 일정의 모든 좌석 조회
    public List<Seat> getSeatsBySchedule(Long scheduleId) {
        return seatRepository.findByScheduleIdOrderBySeatNoAsc(scheduleId);
    }

    // 좌석 예약 (동시성 제어)
    @Transactional
    public Seat reserveSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // 상태 체크 추가
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }

        try {
            seat.reserve(userId);
            return seatRepository.save(seat);
        } catch (OptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    // 결제 완료
    @Transactional
    public void confirmSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // 본인이 예약한 좌석인지 확인
        if (!userId.equals(seat.getUserId())) {
            throw new CustomException(ErrorCode.SEAT_NOT_OWNED);
        }

        seat.confirmSold();
    }

    // 예약 취소
    @Transactional
    public void cancelReservation(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // 본인이 예약한 좌석인지 확인
        if (!userId.equals(seat.getUserId())) {
            throw new CustomException(ErrorCode.SEAT_NOT_OWNED);
        }

        seat.cancelReservation();
    }
}
