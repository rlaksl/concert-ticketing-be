package com.ticket.global.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            LocalDateTime.now(),
            errorCode.getHttpStatus().value(),
            errorCode.getHttpStatus().name(),
            errorCode.getMessage()
        );
    }

    // 커스텀 메시지용 (유효성 검증 실패 시)
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getHttpStatus().value(),
                errorCode.getHttpStatus().name(),
                message
        );
    }
}
