package com.roomreservationservice.infrastructure.client;


import com.roomreservationservice.dto.ErrorResponse;
import com.roomreservationservice.dto.PaymentStatusResponse;
import com.roomreservationservice.dto.PaymentStatusRetrievalRequest;
import com.roomreservationservice.enums.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CreditCardPaymentClient {

    private final WebClient webClient;
    private static final String PAYMENT_SERVICE_URL = "http://localhost:9090/host/credit-card-payment-api/payment-status";


    public CreditCardPaymentClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean validatePayment(String paymentReference) {
        PaymentStatusRetrievalRequest request = new PaymentStatusRetrievalRequest(paymentReference);

        PaymentStatusResponse response = webClient.post()
                .uri(PAYMENT_SERVICE_URL)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                        clientResponse -> clientResponse.bodyToMono(ErrorResponse.class)
                                .map(error -> new IllegalStateException(
                                        "Invalid Input: " + error.error()
                                ))
                )
                .onStatus(
                        status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        clientResponse -> clientResponse.bodyToMono(ErrorResponse.class)
                                .map(error -> new IllegalStateException(
                                        "Payment not found: " + error.error()
                                ))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(ErrorResponse.class)
                                .map(error -> new IllegalStateException(
                                        "Credit card service failure: " + error.error()
                                ))
                )
                .bodyToMono(PaymentStatusResponse.class)
                .toFuture()
                .join();

        if (response == null) {
            throw new IllegalStateException("No response from credit card payment service");
        }

        return response.status() == PaymentStatus.CONFIRMED;
    }

}