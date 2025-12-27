package com.ticket.domain.seat.controller;

import com.ticket.domain.seat.dto.SeatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SeatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 /app/seats/select 로 메시지 보내면 실행
    @MessageMapping("/seats/select")
    public void selectSeat(@Payload SeatMessage message) {
        // 해당 일정을 구독 중인 모든 클라이언트에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/seats/" + message.getScheduleId(),
                message
        );
    }
}
