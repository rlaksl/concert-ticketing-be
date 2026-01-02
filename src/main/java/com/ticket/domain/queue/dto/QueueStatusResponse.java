package com.ticket.domain.queue.dto;

public record QueueStatusResponse(
        Long scheduleId, // 공연 일정 ID
        Long position,
        Long totalWaiting,
        QueueStatus status, // 상태
        Long estimatedWaitSeconds, // 예상 대기시간
        String entryToken // 입장 토큰 (READY 상태일 때만 값 있음)
) {
   // 대기 중 상태 응담 생성
   public static QueueStatusResponse waiting(Long scheduleId, Long position, Long totalWaiting, Long estimatedWaitSeconds) {
       return new QueueStatusResponse(
               scheduleId,
               position,
               totalWaiting,
               QueueStatus.WAITING,
               estimatedWaitSeconds,
               null
       );
   }

   // 입장 가능 상태 응답 생성
    public static QueueStatusResponse ready(Long scheduleId, String entryToken) {
        return new QueueStatusResponse(
                scheduleId,
                0L,
                0L,
                QueueStatus.READY,
                0L,
                entryToken
        );
    }

    // 만료 상태 응답 생성
    public static QueueStatusResponse expired(Long scheduleId) {
       return  new QueueStatusResponse(
               scheduleId,
               0L,
               0L,
               QueueStatus.EXPIRED,
               0L,
               null
       );
    }
}
