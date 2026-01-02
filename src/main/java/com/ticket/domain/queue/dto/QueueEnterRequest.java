package com.ticket.domain.queue.dto;

import jakarta.validation.constraints.NotNull;

public record QueueEnterRequest(
        @NotNull(message = "일정 ID는 필수입니다.")
        Long scheduleId
) {
}
