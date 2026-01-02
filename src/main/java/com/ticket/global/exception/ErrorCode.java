package com.ticket.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),

    // 회원
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // 공연
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 일정을 찾을 수 없습니다."),
    BOOKING_NOT_AVAILABLE(HttpStatus.FORBIDDEN, "예매 오픈 시간 전입니다."),

    // 좌석
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "다른 사용자가 먼저 예약한 좌석입니다."),
    SEAT_NOT_OWNED(HttpStatus.FORBIDDEN, "본인이 예약한 좌석이 아닙니다."),
    SEAT_NOT_RESERVED(HttpStatus.BAD_REQUEST, "예약된 좌석이 아닙니다."),

    // 대기열
    INVALID_ENTRY_TOKEN(HttpStatus.FORBIDDEN, "유효하지 않은 입장 토큰입니다."),
    ENTRY_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "입장 토큰이 만료되었습니다. 대기열에 다시 등록해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
