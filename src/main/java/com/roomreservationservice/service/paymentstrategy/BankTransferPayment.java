package com.roomreservationservice.service.paymentstrategy;

import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.model.Reservation;
import org.springframework.stereotype.Service;

@Service
public class BankTransferPayment implements PaymentStrategy {
    @Override
    public ReservationStatus processPayment(Reservation reservation) {
        return ReservationStatus.PENDING_PAYMENT; // event-driven
    }
}
