package com.roomreservationservice.service.paymentstrategy;

import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.model.Reservation;

public interface PaymentStrategy {
    ReservationStatus processPayment(Reservation reservation);
}