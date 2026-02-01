package com.roomreservationservice.dto;

import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.RoomSegment;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ReservationRequest(
        @Min(1)
        int roomNumber,

        @NotBlank(message = "Customer name cannot be blank")
        String customerName,

        @NotNull(message = "Reservation start date is required")
        @FutureOrPresent(message = "Reservation start date cannot be in the past")
        LocalDate reservationStartDate,

        @NotNull(message = "Reservation end date is required")
        @Future(message = "Reservation end date must be in the future")
        LocalDate reservationEndDate,

        @NotNull(message = "Room segment is required")
        RoomSegment roomSegment,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode,

        String paymentReference, // optional for Bank Transfer, required for Credit Card

        @NotNull(message = "Total Amount mode is required")
        @PositiveOrZero(message = "Total amount must be greater than or equal to zero")
        long totalAmount

        ) {
}

