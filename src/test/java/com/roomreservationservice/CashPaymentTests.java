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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CashPaymentTests {
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
    void should_confirm_reservation_when_payment_is_cash() {

        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(2),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CASH,
                        null,
                        100
                );

        ReservationResponse response = reservationService.confirmReservation(request);
        assertEquals(ReservationStatus.CONFIRMED, response.reservationStatus());
    }

    @Test
    void should_error_illegal_argument_exception_reservation_when_payment_is_cash() {

        ReservationRequest request =
                new ReservationRequest(
                        1,
                        "Berkan",
                        LocalDate.now(),
                        LocalDate.now().plusDays(45),
                        RoomSegment.EXTRA_LARGE,
                        PaymentMode.CASH,
                        null,
                        100
                );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reservationService.confirmReservation(request));
        assertTrue(exception.getMessage().contains("Reservations cannot be more than 30 days."));
    }


}
