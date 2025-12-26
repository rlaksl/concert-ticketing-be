package com.ticket.global.config;

import com.ticket.domain.concert.entity.Concert;
import com.ticket.domain.concert.entity.ConcertSchedule;
import com.ticket.domain.concert.repository.ConcertRepository;
import com.ticket.domain.concert.repository.ConcertScheduleRepository;
import com.ticket.domain.seat.entity.Seat;
import com.ticket.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    @Override
    public void run(String... args) {
        // 이미 데이터 있으면 스킵
        if (concertRepository.count() > 0) {
            return;
        }

        // 공연 1: 최강창민 콘서트
        Concert concert1 = concertRepository.save(
                Concert.builder()
                        .title("2026 최강창민 콘서트")
                        .artist("최강창민")
                        .build()
        );

        // 일정 1: 예매 가능 (bookingAvailableAt을 과거로)
        ConcertSchedule schedule1 = scheduleRepository.save(
                ConcertSchedule.builder()
                        .concert(concert1)
                        .concertAt(LocalDateTime.of(2026, 2, 18, 16, 0))
                        .bookingAvailableAt(LocalDateTime.of(2025, 1, 1, 0, 0)) // 과거 = 예매 가능
                        .totalSeats(30000)
                        .build()
        );

        for (int i = 1; i <= 10000; i++) {
            seatRepository.save(
                    Seat.builder()
                            .schedule(schedule1)
                            .seatNo(i)
                            .price(150000)
                            .build()
            );
        }

        // 공연 2: 최강창민 앵콜 콘서트
        Concert concert2 = concertRepository.save(
                Concert.builder()
                        .title("2026 최강창민 앵콜 콘서트")
                        .artist("최강창민")
                        .build()
        );

        // 일정 2: 예매 불가 (bookingAvailableAt을 미래로)
        ConcertSchedule schedule2 = scheduleRepository.save(
                ConcertSchedule.builder()
                        .concert(concert2)
                        .concertAt(LocalDateTime.of(2026, 4, 26, 16, 0))
                        .bookingAvailableAt(LocalDateTime.of(2026, 3, 1, 20, 0)) // 미래 = 예매 불가
                        .totalSeats(70000)
                        .build()
        );

        for (int i = 1; i <= 10000; i++) {
            seatRepository.save(
                    Seat.builder()
                            .schedule(schedule2)
                            .seatNo(i)
                            .price(170000)
                            .build()
            );
        }

        System.out.println("테스트 데이터 초기화 완료!");
    }
}