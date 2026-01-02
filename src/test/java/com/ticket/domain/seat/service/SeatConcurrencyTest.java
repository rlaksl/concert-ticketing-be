package com.ticket.domain.seat.service;

import com.ticket.domain.concert.entity.Concert;
import com.ticket.domain.concert.entity.ConcertSchedule;
import com.ticket.domain.concert.repository.ConcertRepository;
import com.ticket.domain.concert.repository.ConcertScheduleRepository;
import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.entity.SeatStatus;
import com.ticket.domain.seat.repository.SeatRepository;
import com.ticket.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Disabled;



import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeatConcurrencyTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository scheduleRepository;

    private Long testSeatId;

    // 테스트 실행 전 테스트용 데이터 생성
    // 공연 -> 일정 -> 좌석 순서로 생성(FK 관계)

    @BeforeEach
    void setUp() {
        // 테스트용 공연
        Concert concert = Concert.builder()
                .title("테스트 콘서트")
                .artist("테스트 아티스트")
                .build();
        concertRepository.save(concert);

        // 테스트용 일정
        ConcertSchedule schedule = ConcertSchedule.builder()
                .concert(concert)
                .concertAt(LocalDateTime.now().plusDays(30))
                .bookingAvailableAt(LocalDateTime.now().minusDays(1))
                .totalSeats(100)
                .build();
        scheduleRepository.save(schedule);

        // 테스트용 좌석 생성 (동시성 테스트 대상)
        Seat seat = Seat.builder()
                .schedule(schedule)
                .seatNo(1)
                .price(100000)
                .build();
        seatRepository.save(seat);

        testSeatId = seat.getId();
    }

    @Test
    @Disabled("대기열 시스템 추가로 인해 테스트 방식 변경 필요")
    @DisplayName("10000명이 동시에 같은 좌석 예약 시 1명만 성공")
    void concurrentReservation_onlyOneSucceeds() throws InterruptedException {
        // given - 테스트 환경 설정
        int threadCount = 10000;

        // ExecutorService: 스레드 풀 생성 (최대 32개 스레드가 동시 실행)
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch: 100개 작업이 모두 끝날 때까지 메인 스레드 대기시킴
        CountDownLatch latch = new CountDownLatch(threadCount);

        // AtomicInteger: 멀티스레드 환경에서 안전하게 카운트 (synchronized 없이)
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명이 동시에 같은 좌석 예약 시도
        for(int i = 0; i < threadCount; i++) {
            long userId = i + 1; // 각각 다른 사용자 ID

            executorService.submit(() -> {
                try {
                    seatService.reserveSeat(testSeatId, userId);
                    successCount.incrementAndGet(); // 성공 시 +1
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 실패 시 +1;
                    System.out.println("예약 실패 [userId=" + userId + "]: " + e.getMessage());
                } finally {
                    latch.countDown();  // 작업 완료 신호 (100 → 99 → 98 → ... → 0)
                }
            });
        }

        // 모든 스레드 완료 대기 (latch가 0이 될 때까지)
        latch.await();

        // 스레드 풀 종료 (리소스 정리)
        executorService.shutdown();

        // then - 검증
        System.out.println("========== 테스트 결과 ==========");
        System.out.println("성공: " + successCount.get() + "명");
        System.out.println("실패: " + failCount.get() + "명");

        // 1명만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9999);

        // 좌석 상태가 TEMPORARY로 변경되었는지 확인
        Seat seat = seatRepository.findById(testSeatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.TEMPORARY);
    }

}