package com.example.payment;

/**
 * ゲートウェイからのレスポンスが想定外の形だったときの例外。（bad/ と同じ）
 * これは「生の（低レベルの）例外」で、上のサービスがシステム系の独自例外に翻訳する。
 */
public class GatewayParseException extends Exception {

    public GatewayParseException(String message) {
        super(message);
    }
}
