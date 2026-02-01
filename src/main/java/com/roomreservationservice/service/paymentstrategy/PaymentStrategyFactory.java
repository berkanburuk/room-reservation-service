package com.roomreservationservice.service.paymentstrategy;

import com.roomreservationservice.enums.PaymentMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMode, PaymentStrategy> strategyMap;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = new HashMap<>();
        strategies.forEach(strategy -> {
            if (strategy instanceof CashPayment) strategyMap.put(PaymentMode.CASH, strategy);
            if (strategy instanceof CreditCardPayment) strategyMap.put(PaymentMode.CREDIT_CARD, strategy);
            if (strategy instanceof BankTransferPayment) strategyMap.put(PaymentMode.BANK_TRANSFER, strategy);
        });
    }

    public PaymentStrategy getStrategy(PaymentMode paymentMode) {
        return strategyMap.get(paymentMode);
    }
}