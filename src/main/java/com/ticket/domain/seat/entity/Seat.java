package com.ticket.domain.seat.entity;

import com.ticket.domain.concert.entity.ConcertSchedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    @Column(name = "seat_no", nullable = false)
    private Integer seatNo;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "user_id")
    private Long userId;

    @Version
    private Long version;

    @Builder
    public Seat(ConcertSchedule schedule, Integer seatNo, Integer price) {
        this.schedule = schedule;
        this.seatNo = seatNo;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    // 좌석 임시 점유
    public void reserve(Long userId) {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        this.status = SeatStatus.TEMPORARY;
        this.userId = userId;
    }

    // 결제 완료
    public void confirmSold() {
        this.status = SeatStatus.SOLD;
    }

    // 예매 취소
    public void cancelReservation() {
        this.status = SeatStatus.AVAILABLE;
        this.userId = null;
    }
}