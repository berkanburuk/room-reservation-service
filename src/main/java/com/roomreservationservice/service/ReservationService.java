package com.roomreservationservice.service;

import com.roomreservationservice.dto.ReservationRequest;
import com.roomreservationservice.dto.ReservationResponse;
import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.exception.RoomAlreadyBookedException;
import com.roomreservationservice.infrastructure.event.BankTransferPaymentEvent;
import com.roomreservationservice.model.Reservation;
import com.roomreservationservice.repository.ReservationRepository;
import com.roomreservationservice.repository.projection.ReservationSummary;
import com.roomreservationservice.service.paymentstrategy.PaymentStrategy;
import com.roomreservationservice.service.paymentstrategy.PaymentStrategyFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public ReservationService(ReservationRepository reservationRepository, PaymentStrategyFactory paymentStrategyFactory) {
        this.reservationRepository = reservationRepository;
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    @Transactional
    public ReservationResponse confirmReservation(@Valid ReservationRequest reservationRequest) {
        boolean maxReservationDays = reservationRequest.reservationEndDate().isAfter(reservationRequest.reservationStartDate().plusDays(30));
        if (maxReservationDays) {
            throw new IllegalArgumentException("Reservations cannot be more than 30 days.");
        }

        boolean roomOccupied = reservationRepository.existsByRoomNumberAndDateRange(
                reservationRequest.roomNumber(),
                reservationRequest.reservationStartDate(),
                reservationRequest.reservationEndDate()
        );

        if (roomOccupied) {
            throw new RoomAlreadyBookedException("Room " + reservationRequest.roomNumber() +
                    " is already booked in the selected period.");
        }


        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(reservationRequest.paymentMode());
        Reservation reservation = setReservation(reservationRequest, strategy);

        reservationRepository.save(reservation);
        return new ReservationResponse(reservation.getId(), reservation.getStatus());
    }

    private Reservation setReservation(ReservationRequest reservationRequest, PaymentStrategy strategy) {
        Reservation reservation = new Reservation();
        reservation.setRoomNumber(reservationRequest.roomNumber());
        reservation.setCustomerName(reservationRequest.customerName());
        reservation.setStartDate(reservationRequest.reservationStartDate());
        reservation.setEndDate(reservationRequest.reservationEndDate());
        reservation.setRoomSegment(reservationRequest.roomSegment());
        reservation.setPaymentMode(reservationRequest.paymentMode());
        reservation.setPaymentReference(reservationRequest.paymentReference());
        reservation.setTotalAmount(reservationRequest.totalAmount());
        reservation.setStatus(strategy.processPayment(reservation));
        return reservation;
    }

    @Transactional
    public boolean cancelPendingBankTransfers(int daysFromNow) {
        LocalDate targetDate = LocalDate.now().plusDays(daysFromNow);
        List<ReservationSummary> pendingReservationList =
                reservationRepository.findPendingBankTransfers(
                        targetDate,
                        PaymentMode.BANK_TRANSFER,
                        ReservationStatus.PENDING_PAYMENT
                );

        List<Long> idList = pendingReservationList.stream()
                .map(ReservationSummary::id).collect(Collectors.toList());
        log.info("Found {} pending bank transfers to cancel", idList);
        int updatedRows = reservationRepository.updateStatusByIds(idList, ReservationStatus.CANCELLED);
        return updatedRows > 0;
    }

    @Transactional
    public boolean handleBankTransferPayment(BankTransferPaymentEvent event) {

        String[] parts = event.transactionDescription().split(" ");
        if (parts.length < 2 || !parts[1].startsWith("P")) {
            log.error("Invalid transaction description: {}", event.transactionDescription());
            return false;
        }

        long reservationId;
        try {
            reservationId = Long.parseLong(parts[1].substring(1)); // remove 'P'
        } catch (NumberFormatException ex) {
            log.error("Invalid reservation id in transaction description: {}", event.transactionDescription());
            return false;
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        if (reservation == null) {
            log.error("Reservation not found for id {}", reservationId);
            return false;
        }

        // Idempotency guard
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            log.info("Ignoring payment event for reservation {}, status={}", reservationId, reservation.getStatus());
            return true;
        }

        if (event.amountReceived() == reservation.getTotalAmount()) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            log.info("Reservation {} CONFIRMED via bank transfer", reservationId);
            return true;
        }

        log.warn("Bank transfer payment amount mismatch for reservation {}, expected={}, received={}",
                reservationId, reservation.getTotalAmount(), event.amountReceived());

        return false;
    }
}


