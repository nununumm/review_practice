package com.example.payment;

/**
 * ゲートウェイからのレスポンスが想定外の形だったときに投げられる例外。
 */
public class GatewayParseException extends Exception {

    public GatewayParseException(String message) {
        super(message);
    }
}
