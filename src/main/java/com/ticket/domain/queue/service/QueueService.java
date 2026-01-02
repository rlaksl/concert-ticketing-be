package com.ticket.domain.queue.service;

import com.ticket.domain.queue.dto.QueueStatusResponse;
import com.ticket.global.config.QueueProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 추가
    private final QueueProperties queueProperties; // 대기열

    // 레디스 키 패턴
    private static final String WAITING_KEY = "queue:waiting:%d";
    private static final String ACTIVE_KEY = "queue:active:%d";
    private static final String TOKEN_KEY = "entry-token:%d:%d";
    private static final String ACTIVE_SCHEDULES_KEY = "queue:active-schedules";

    // 예상 대기 시간 계산용 (100명당 60초)
    private static final long ESTIMATED_SECONDS_PER_100_USERS = 60L;

    // 대기열 등록
    public QueueStatusResponse enterQueue(Long scheduleId, Long userId) {
        String waitingKey = getWaitingKey(scheduleId);
        String activeKey = getActiveKey(scheduleId);

        // 이미 활성화 상태면 토큰과 함께 반환
        if (isActiveUser(activeKey, userId)) {
            log.info("이미 입장한 사용자: scheduleId={}, userId={}", scheduleId, userId);
            String token = getOrCreateToken(scheduleId, userId);
            return QueueStatusResponse.ready(scheduleId, token);
        }

        // 현재 Active 인원 확인
        Long activeCount = redisTemplate.opsForZSet().zCard(activeKey);
        if (activeCount == null) activeCount = 0L;

        // 여유 있으면 즉시 입장 처리
        if (activeCount < queueProperties.getMaxActiveUsers()) {
            // Active에 추가 (score = 만료 시간)
            long ttlSeconds = queueProperties.getEntryTokenTtlSeconds();
            double expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
            redisTemplate.opsForZSet().add(activeKey, userId.toString(), expireTime);

            // 토큰 발급
            String token = getOrCreateToken(scheduleId, userId);

            log.info("즉시 입장 처리: scheduleId={}, userId={}", scheduleId, userId);
            return QueueStatusResponse.ready(scheduleId, token);
        }

        // 대기열에 등록 (이미 있으면 무시)
        redisTemplate.opsForZSet().addIfAbsent(waitingKey, userId.toString(), System.currentTimeMillis());

        // 활성 스케줄 목록에 추가 (스케줄러가 처리할 대상)
        redisTemplate.opsForSet().add(ACTIVE_SCHEDULES_KEY, scheduleId.toString());

        log.info("대기열 등록: scheduleId={}, userId={}", scheduleId, userId);

        // 현재 상태 반환
        return getQueueStatus(scheduleId, userId);
    }

    // 현재 상태 조회 (읽기 전용 - 입장 처리는 스케줄러가 담당)
    public QueueStatusResponse getQueueStatus(Long scheduleId, Long userId) {
        String waitingKey = getWaitingKey(scheduleId);
        String activeKey = getActiveKey(scheduleId);

        // 1. 활성화 상태 확인
        if (isActiveUser(activeKey, userId)) {
            String token = getOrCreateToken(scheduleId, userId);
            return QueueStatusResponse.ready(scheduleId, token);
        }

        // 2. 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(waitingKey, userId.toString());
        if (rank == null) {
            return QueueStatusResponse.expired(scheduleId);
        }

        // 3. 대기 정보 반환
        long position = rank + 1;
        Long totalWaiting = redisTemplate.opsForZSet().zCard(waitingKey);
        if (totalWaiting == null) totalWaiting = 0L;

        long estimatedSeconds = (position / 100) * ESTIMATED_SECONDS_PER_100_USERS;

        return QueueStatusResponse.waiting(scheduleId, position, totalWaiting, estimatedSeconds);
    }

    // 대기열 이탈
    public void exitQueue(Long scheduleId, Long userId) {
        String waitingKey = getWaitingKey(scheduleId);
        String activeKey = getActiveKey(scheduleId);
        String tokenKey = getTokenKey(scheduleId, userId);

        redisTemplate.opsForZSet().remove(waitingKey, userId.toString());
        redisTemplate.opsForZSet().remove(activeKey, userId.toString());
        redisTemplate.delete(tokenKey);

        log.info("대기열 이탈: scheduleId={}, userId={}", scheduleId, userId);
    }

    // 입장 토큰 검증
    public boolean validateEntryToken(Long scheduleId, Long userId, String token) {
        String tokenKey = getTokenKey(scheduleId, userId);
        String savedToken = redisTemplate.opsForValue().get(tokenKey);
        return token != null && token.equals(savedToken);
    }

    // 스케줄러: 주기적 실행 - 만료 정리 + 입장 처리 + WebSocket 알림
    @Scheduled(fixedDelayString = "${queue.scheduler-interval-ms}")
    public void processQueue() {
        // 활성화된 모든 scheduleId 조회
        Set<String> activeSchedules = redisTemplate.opsForSet().members(ACTIVE_SCHEDULES_KEY);

        if (activeSchedules == null || activeSchedules.isEmpty()) {
            return;
        }

        for (String scheduleIdStr : activeSchedules) {
            Long scheduleId = Long.valueOf(scheduleIdStr);
            boolean hasChanges = processScheduleQueue(scheduleId);

            // 변경사항이 있으면 WebSocket으로 갱신 신호 전송
            if (hasChanges) {
                broadcastQueueUpdate(scheduleId);
            }
        }
    }

    // 특정 일정의 대기열 처리 (변경 여부 반환)
    private boolean processScheduleQueue(Long scheduleId) {
        String waitingKey = getWaitingKey(scheduleId);
        String activeKey = getActiveKey(scheduleId);
        boolean hasChanges = false;

        // 1. 만료된 Active 유저 정리
        Long removedCount = redisTemplate.opsForZSet().removeRangeByScore(activeKey, 0, System.currentTimeMillis());
        if (removedCount != null && removedCount > 0) {
            log.info("만료 유저 정리: scheduleId={}, count={}", scheduleId, removedCount);
            hasChanges = true;
        }

        // 2. 현재 Active 인원 확인
        Long activeCount = redisTemplate.opsForZSet().zCard(activeKey);
        if (activeCount == null) activeCount = 0L;

        // 3. 빈자리 계산
        long availableSlots = queueProperties.getMaxActiveUsers() - activeCount;
        if (availableSlots <= 0) {
            return hasChanges;
        }

        // 4. 대기열에서 빈자리만큼 입장 처리
        Set<String> waitingUsers = redisTemplate.opsForZSet().range(waitingKey, 0, availableSlots - 1);
        if (waitingUsers == null || waitingUsers.isEmpty()) {
            // 대기열이 비었으면 활성 스케줄 목록에서 제거
            Long waitingCount = redisTemplate.opsForZSet().zCard(waitingKey);
            if (waitingCount == null || waitingCount == 0) {
                redisTemplate.opsForSet().remove(ACTIVE_SCHEDULES_KEY, scheduleId.toString());
            }
            return hasChanges;
        }

        for (String userIdStr : waitingUsers) {
            Long userId = Long.valueOf(userIdStr);
            processEntry(scheduleId, userId, waitingKey, activeKey);
            hasChanges = true;
        }

        return hasChanges;
    }

    // 입장 처리 (대기열 → Active 이동 + 토큰 발급)
    private void processEntry(Long scheduleId, Long userId, String waitingKey, String activeKey) {
        // 대기열에서 제거
        redisTemplate.opsForZSet().remove(waitingKey, userId.toString());

        // Active에 추가 (score = 만료 시간)
        long ttlSeconds = queueProperties.getEntryTokenTtlSeconds();
        double expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        redisTemplate.opsForZSet().add(activeKey, userId.toString(), expireTime);

        // 토큰 발급
        getOrCreateToken(scheduleId, userId);

        log.info("입장 처리: scheduleId={}, userId={}", scheduleId, userId);
    }

    // WebSocket 갱신 신호 브로드캐스트
    private void broadcastQueueUpdate(Long scheduleId) {
        String destination = "/topic/queue/" + scheduleId;
        messagingTemplate.convertAndSend(destination, "UPDATE");
        log.debug("대기열 갱신 신호 전송: scheduleId={}", scheduleId);
    }

    // Active 상태 확인
    private boolean isActiveUser(String activeKey, Long userId) {
        Double score = redisTemplate.opsForZSet().score(activeKey, userId.toString());
        return score != null;
    }

    // 토큰 조회 또는 생성
    private String getOrCreateToken(Long scheduleId, Long userId) {
        String tokenKey = getTokenKey(scheduleId, userId);
        String token = redisTemplate.opsForValue().get(tokenKey);

        if (token == null) {
            token = UUID.randomUUID().toString();
            long ttlSeconds = queueProperties.getEntryTokenTtlSeconds();
            redisTemplate.opsForValue().set(tokenKey, token, Duration.ofSeconds(ttlSeconds));
        }

        return token;
    }

    // Key 생성 헬퍼 메서드
    private String getWaitingKey(Long scheduleId) {
        return String.format(WAITING_KEY, scheduleId);
    }

    private String getActiveKey(Long scheduleId) {
        return String.format(ACTIVE_KEY, scheduleId);
    }

    private String getTokenKey(Long scheduleId, Long userId) {
        return String.format(TOKEN_KEY, scheduleId, userId);
    }
}
