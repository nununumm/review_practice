package com.example.payment;

/**
 * 決済結果。（bad/ と同じ）
 * 決済ゲートウェイが返してくる取引ID（transactionId）と、承認されたか（approved）を持つ。
 */
public class PaymentResult {

    private final String transactionId;
    private final boolean approved;

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
