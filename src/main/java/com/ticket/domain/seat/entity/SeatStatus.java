package com.ticket.domain.seat.entity;

public enum  SeatStatus {
    AVAILABLE, // 예매 가능
    TEMPORARY, // 임시 점유 (결제 대기)
    SOLD // 판매 완료
}