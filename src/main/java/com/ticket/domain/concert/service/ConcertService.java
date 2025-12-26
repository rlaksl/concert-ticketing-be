package com.ticket.domain.concert.service;

import com.ticket.domain.concert.entity.Concert;
import com.ticket.domain.concert.repository.ConcertRepository;
import com.ticket.global.exception.CustomException;
import com.ticket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;

    // 공연 전체 조회
    public List<Concert> getAllConcerts() {
        return concertRepository.findAll();
    }

    // 공연 단건 조회
    public Concert getConcert(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));
    }
}
