package com.ticket.domain.seat.dto;

import com.ticket.domain.seat.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatMessage {

    private Long seatId;
    private Long scheduleId;
    private SeatStatus status;
    private String action;  // "SELECT", "RESERVE", "CANCEL"

    public static SeatMessage of(Long seatId, Long scheduleId, SeatStatus status, String action) {
        return new SeatMessage(seatId, scheduleId, status, action);
    }

}