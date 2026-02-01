package com.roomreservationservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationExpirationScheduler {

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // everynight at midnight
    public void schedule() {
        int DAYS_FROM_NOW = 2;
        boolean success = reservationService.cancelPendingBankTransfers(DAYS_FROM_NOW);
        log.info("Scheduled cancellation executed. Success: {}", success);
    }


}
