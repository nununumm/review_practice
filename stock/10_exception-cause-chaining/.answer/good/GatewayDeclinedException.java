package com.example.payment;

/**
 * 残高不足・カード無効・限度額超過など、ゲートウェイに決済を断られたときの例外。（bad/ と同じ）
 * これも「生の（低レベルの）例外」。上のサービスが業務系の独自例外に翻訳する。
 */
public class GatewayDeclinedException extends Exception {

    private final String reason;

    public GatewayDeclinedException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
