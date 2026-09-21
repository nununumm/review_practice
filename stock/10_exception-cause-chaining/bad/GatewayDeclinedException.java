package com.example.payment;

/**
 * 残高不足・カード無効・限度額超過など、ゲートウェイ側に「決済を断られた」ときの例外。
 * reason に "INSUFFICIENT_FUNDS" などの理由コードが入る。
 */
public class GatewayDeclinedException extends Exception {

    private String reason;

    public GatewayDeclinedException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
