package com.ticket.domain.queue.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QueueWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 대기열 구독 시작 알림 (선택적)
    @MessageMapping("/queue/subscribe/{scheduleId}")
    public void subscribeQueue(@DestinationVariable Long scheduleId) {
        log.info("대기열 구독 시작: scheduleId={}", scheduleId);
    }
}
