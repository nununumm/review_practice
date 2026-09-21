package com.example.payment;

/**
 * 決済結果。決済ゲートウェイが返してくる取引ID（transactionId）などを保持する。
 */
public class PaymentResult {

    private String transactionId;
    private boolean approved;

    public PaymentResult(String transactionId, boolean approved) {
        this.transactionId = transactionId;
        this.approved = approved;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isApproved() {
        return approved;
    }
}
