package com.ticket.domain.queue.dto;

public enum QueueStatus {
    WAITING, // 대기 중 (순번 대기)
    READY, // 입장 가능 (토큰 발급됨)
    ENTERED, // 입장 완료 (좌석 선택 페이지에 있음)
    EXPIRED // 만료됨 (5분 초과 또는 이탈)
}
