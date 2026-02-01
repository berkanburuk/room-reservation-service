package com.roomreservationservice;

import com.roomreservationservice.dto.ReservationRequest;
import com.roomreservationservice.dto.ReservationResponse;
import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.enums.RoomSegment;
import com.roomreservationservice.infrastructure.client.CreditCardPaymentClient;
import com.roomreservationservice.infrastructure.event.BankTransferPaymentEvent;
import com.roomreservationservice.model.Reservation;
import com.roomreservationservice.repository.ReservationRepository;
import com.roomreservationservice.repository.projection.ReservationSummary;
import com.roomreservationservice.service.ReservationService;
import com.roomreservationservice.service.paymentstrategy.BankTransferPayment;
import com.roomreservationservice.service.paymentstrategy.CashPayment;
import com.roomreservationservice.service.paymentstrategy.CreditCardPayment;
import com.roomreservationservice.service.paymentstrategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)

public class BankTransferPaymentTests {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CreditCardPaymentClient creditCardPaymentClient;

    private ReservationService reservationService;

    @Mock
    BankTransferPaymentEvent event;

    @BeforeEach
    void setUp() {
        PaymentStrategyFactory factory =
                new PaymentStrategyFactory(List.of(
                        new CashPayment(),
                        new CreditCardPayment(creditCardPaymentClient),
                        new BankTransferPayment()
                ));

        reservationService = new ReservationService(
                reservationRepository,
                factory
        );

    }

    @Test
    void should_pending_reservation_when_payment_is_bank_transfer() {

        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.BANK_TRANSFER,
                        null,
                        100
                );

        ReservationResponse response = reservationService.confirmReservation(request);
        assertEquals(ReservationStatus.PENDING_PAYMENT, response.reservationStatus());
    }

    @Test
    void should_confirm_bank_transfer_payment_when_amount_is_full() {
        event = new BankTransferPaymentEvent(
                "Attribute Description Test",
                1L,
                123,
                250,
                "1401541457 P4145478"
        );
        Reservation reservation = getReservation();
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        Mockito.when(reservationRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(reservation));

        boolean result = reservationService.handleBankTransferPayment(event);
        assertTrue(result);
    }

    @Test
    void should_keep_pending_when_bank_transfer_amount_is_partial() {
        event = new BankTransferPaymentEvent(
                "Attribute Description Test",
                1L,
                123,
                100,
                "1401541457 P4145478"
        );

        Reservation reservation = getReservation();
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        Mockito.when(reservationRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(reservation));

        boolean result = reservationService.handleBankTransferPayment(event);
        assertFalse(result);

    }

    private Reservation getReservation() {
        Reservation reservation = new Reservation();
        reservation.setRoomNumber(1);
        reservation.setCustomerName("Alex");
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));
        reservation.setRoomSegment(RoomSegment.MEDIUM);
        reservation.setPaymentMode(PaymentMode.BANK_TRANSFER);
        reservation.setPaymentReference("4145478");
        reservation.setTotalAmount(250);
        return reservation;
    }

    @Test
    void should_call_cancelPendingBankTransfers() {
        List<ReservationSummary> reservationSummaryList =
                List.of(new ReservationSummary(1L, LocalDate.now().plusDays(1), ReservationStatus.PENDING_PAYMENT),
                        new ReservationSummary(2L, LocalDate.now().plusDays(1), ReservationStatus.PENDING_PAYMENT),
                        new ReservationSummary(3L, LocalDate.now().plusDays(5), ReservationStatus.PENDING_PAYMENT)
                );

        Mockito.when(reservationRepository.findPendingBankTransfers(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(reservationSummaryList);
        Mockito.when(reservationRepository.updateStatusByIds(Mockito.any(), Mockito.any())).thenReturn(3);

        boolean result = reservationService.cancelPendingBankTransfers(2);
        assertTrue(result);
    }

}
