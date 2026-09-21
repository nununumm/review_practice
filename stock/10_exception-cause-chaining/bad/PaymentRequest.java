package com.example.payment;

import java.math.BigDecimal;

/**
 * 決済のリクエスト情報。
 * どの注文（orderId）を、いくら（amount）、どのカード（cardToken）で払うか。
 */
public class PaymentRequest {

    private String orderId;
    private BigDecimal amount;
    private String cardToken;

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
