package com.roomreservationservice.infrastructure.messaging;

import com.roomreservationservice.infrastructure.event.BankTransferPaymentEvent;
import com.roomreservationservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Profile("kafka")
@RequiredArgsConstructor
public class BankTransferPaymentListener {

    private final ReservationService reservationService;

    @KafkaListener(topics = "bank-transfer-payment-update")
    public void onMessage(BankTransferPaymentEvent event) {
        reservationService.handleBankTransferPayment(event);
    }
}