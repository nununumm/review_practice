package com.example.payment;

import java.math.BigDecimal;

/**
 * 決済のリクエスト情報。（bad/ と同じ）
 * どの注文（orderId）を、いくら（amount）、どのカード（cardToken）で払うか。
 */
public class PaymentRequest {

    private final String orderId;
    private final BigDecimal amount;
    private final String cardToken;

    public PaymentRequest(String orderId, BigDecimal amount, String cardToken) {
        this.orderId = orderId;
        this.amount = amount;
        this.cardToken = cardToken;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCardToken() {
        return cardToken;
    }
}
