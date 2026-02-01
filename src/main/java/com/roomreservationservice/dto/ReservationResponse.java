package com.roomreservationservice.dto;

import com.roomreservationservice.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record ReservationResponse(
        @NotNull
        long reservationId,

        @NotNull
        ReservationStatus reservationStatus) { }

