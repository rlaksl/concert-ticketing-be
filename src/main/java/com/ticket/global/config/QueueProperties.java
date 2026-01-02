package com.ticket.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "queue")
public class QueueProperties {

    // 동시 입장 가능한 최대 인원
    private int maxActiveUsers = 1000;

    // 입장 토큰 유효 시간 (초)
    private long entryTokenTtlSeconds = 300;

    // 스케줄러 실행 간격 (밀리초)
    private long schedulerIntervalMs = 1000;
}
