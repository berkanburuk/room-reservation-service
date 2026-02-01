package com.roomreservationservice.repository.projection;

import com.roomreservationservice.enums.ReservationStatus;

import java.time.LocalDate;

public record ReservationSummary(
        Long id,
        LocalDate endDate,
        ReservationStatus reservationStatus
) {
}
