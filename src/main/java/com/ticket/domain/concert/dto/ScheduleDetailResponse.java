package com.ticket.domain.concert.dto;

import com.ticket.domain.concert.entity.Concert;
import com.ticket.domain.concert.entity.ConcertSchedule;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleDetailResponse(
        Long scheduleId,
        LocalDateTime concertAt,
        LocalDateTime bookingAvailableAt,
        Integer totalSeats,
        ConcertInfo concert,
        List<OtherScheduleInfo> otherSchedules
) {
    // 공연 정보
    public record ConcertInfo(
            Long id,
            String title,
            String artist
    ) {
        public static ConcertInfo from(Concert concert) {
            return new ConcertInfo(
                    concert.getId(),
                    concert.getTitle(),
                    concert.getArtist()
            );
        }
    }

    // 다른 일정 정보 (드롭다운용)
    public record OtherScheduleInfo(
            Long scheduleId,
            LocalDateTime concertAt,
            boolean isBookingAvailable
    ) {
        public static OtherScheduleInfo from(ConcertSchedule schedule) {
            return new OtherScheduleInfo(
                    schedule.getId(),
                    schedule.getConcertAt(),
                    schedule.isBookingAvailable()
            );
        }
    }

    public static ScheduleDetailResponse of(ConcertSchedule schedule, List<ConcertSchedule> allSchedules) {
        List<OtherScheduleInfo> otherSchedules = allSchedules.stream()
                .map(OtherScheduleInfo::from)
                .toList();

        return new ScheduleDetailResponse(
                schedule.getId(),
                schedule.getConcertAt(),
                schedule.getBookingAvailableAt(),
                schedule.getTotalSeats(),
                ConcertInfo.from(schedule.getConcert()),
                otherSchedules
        );
    }
}
