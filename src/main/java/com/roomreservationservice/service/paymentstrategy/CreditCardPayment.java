package com.roomreservationservice.service.paymentstrategy;

import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.infrastructure.client.CreditCardPaymentClient;
import com.roomreservationservice.model.Reservation;
import org.springframework.stereotype.Service;

@Service
public class CreditCardPayment implements PaymentStrategy {

    private final CreditCardPaymentClient client;

    public CreditCardPayment(CreditCardPaymentClient client) {
        this.client = client;
    }

    @Override
    public ReservationStatus processPayment(Reservation reservation) {
        if (reservation.getPaymentReference() == null || reservation.getPaymentReference().isBlank()) {
            throw new IllegalArgumentException("Payment reference is required for Credit Card payments.");
        }

        boolean success = client.validatePayment(reservation.getPaymentReference());
        if (success) return ReservationStatus.CONFIRMED;
        else throw new IllegalStateException("Credit Card payment failed.");
    }
}