package com.roomreservationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentStatusRetrievalRequest(

        @NotBlank(message = "Payment reference must not be blank")
        String paymentReference

) {
}