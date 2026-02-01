package com.roomreservationservice.dto;

import com.roomreservationservice.enums.PaymentStatus;

import java.time.OffsetDateTime;

public record PaymentStatusResponse(
        OffsetDateTime lastUpdateDate,
        PaymentStatus status
) {
}