package com.ticket.domain.concert.entity;

import com.ticket.domain.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "concert_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(name = "concert_at", nullable = false)
    private LocalDateTime concertAt;  // 실제 공연 날짜/시간

    @Column(name = "booking_available_at", nullable = false)
    private LocalDateTime bookingAvailableAt;  // 예매 오픈 시간

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();

    @Builder
    public ConcertSchedule(Concert concert, LocalDateTime concertAt,
                           LocalDateTime bookingAvailableAt, Integer totalSeats) {
        this.concert = concert;
        this.concertAt = concertAt;
        this.bookingAvailableAt = bookingAvailableAt;
        this.totalSeats = totalSeats;
    }

    // 예매 가능 여부 확인
    public boolean isBookingAvailable() {
        return LocalDateTime.now().isAfter(bookingAvailableAt);
    }
}