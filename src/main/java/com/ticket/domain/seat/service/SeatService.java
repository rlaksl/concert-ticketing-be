package com.ticket.domain.seat.service;

import com.ticket.domain.seat.dto.SeatMessage;
import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.entity.SeatStatus;
import com.ticket.domain.seat.repository.SeatRepository;
import com.ticket.global.exception.CustomException;
import com.ticket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
            Seat savedSeat = seatRepository.save(seat);

            // WebSocket 브로드캐스트 추가
            broadcastSeatUpdate(savedSeat, "RESERVE");

            return savedSeat;
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

        // WebSocket 브로드캐스트 추가
        broadcastSeatUpdate(seat, "CONFIRM");
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

        // WebSocket 브로드캐스트 추가
        broadcastSeatUpdate(seat, "CANCEL");
    }

    // WebSocket 브로드캐스트 메서드
    private void broadcastSeatUpdate(Seat seat, String action) {
        SeatMessage message = SeatMessage.of(
                seat.getId(),
                seat.getSchedule().getId(),
                seat.getStatus(),
                action
        );
        messagingTemplate.convertAndSend(
                "/topic/seats/" + seat.getSchedule().getId(),
                message
        );
    }
}
