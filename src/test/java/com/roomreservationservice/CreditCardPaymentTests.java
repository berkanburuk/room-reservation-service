package com.roomreservationservice;

import com.roomreservationservice.dto.ReservationRequest;
import com.roomreservationservice.dto.ReservationResponse;
import com.roomreservationservice.enums.PaymentMode;
import com.roomreservationservice.enums.ReservationStatus;
import com.roomreservationservice.enums.RoomSegment;
import com.roomreservationservice.infrastructure.client.CreditCardPaymentClient;
import com.roomreservationservice.repository.ReservationRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreditCardPaymentTests {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CreditCardPaymentClient creditCardPaymentClient;

    private ReservationService reservationService;

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
    void should_throw_illegal_argument_exception_reservation_when_payment_is_credit_card() {

        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        null,
                        100
                );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.confirmReservation(request));

        assertTrue(exception.getMessage().contains("Payment reference is required for Credit Card payments."));
    }


    @Test
    void should_throw_illegal_state_exception_reservation_when_payment_is_credit_card() {

        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        "1234",
                        100
                );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> reservationService.confirmReservation(request));
        assertTrue(exception.getMessage().contains("Credit Card payment failed."));

    }

    @Test
    void should_confirm_reservation_when_payment_is_credit_card() {
        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        "4145478",
                        100
                );

        Mockito.when(creditCardPaymentClient.validatePayment("4145478")).thenReturn(true);

        ReservationResponse response = reservationService.confirmReservation(request);
        assertEquals(ReservationStatus.CONFIRMED, response.reservationStatus());
    }

    @Test
    void should_error_bad_request_reservation_when_payment_is_credit_card() {
        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        "4145478",
                        100L
                );


        Mockito.when(creditCardPaymentClient.validatePayment(Mockito.anyString()))
                .thenThrow(new IllegalStateException("Invalid Input"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> reservationService.confirmReservation(request));
        assertTrue(exception.getMessage().contains("Invalid Input"));

    }

    @Test
    void should_error_not_found_reservation_when_payment_is_credit_card() {
        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        "4145478",
                        100L
                );


        Mockito.when(creditCardPaymentClient.validatePayment(Mockito.anyString()))
                .thenThrow(new IllegalArgumentException("Payment not found"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reservationService.confirmReservation(request));
        assertTrue(exception.getMessage().contains("Payment not found"));
    }

    @Test
    void should_server_internal_error_reservation_when_payment_is_credit_card() {
        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CREDIT_CARD,
                        "4145478",
                        100L
                );


        Mockito.when(creditCardPaymentClient.validatePayment(Mockito.anyString()))
                .thenThrow(new IllegalStateException("Credit card service failure"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> reservationService.confirmReservation(request));
        assertTrue(exception.getMessage().contains("Credit card service failure"));
    }


}