package com.roomreservationservice.infrastructure.event;

public record BankTransferPaymentEvent(
        String attribute,
        long paymentId,
        long debtorAccountNumber,
        long amountReceived,
        String transactionDescription
) {

}