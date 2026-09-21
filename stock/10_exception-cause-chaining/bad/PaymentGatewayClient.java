package com.example.payment;

import java.io.IOException;

/**
 * 外部の決済ゲートウェイ（クレジットカード会社の決済API）を叩く低レベルの部品。
 * ネットワーク通信やレスポンスのパースで例外が飛ぶことがある。
 * （ここでは中身は実装済みという想定。シグネチャだけ見ればよい）
 */
public interface PaymentGatewayClient {

    /**
     * ゲートウェイに決済をリクエストする。
     * - 通信に失敗すると IOException を投げる（一時的に落ちているだけかもしれない）
     * - レスポンスの形が想定外だと GatewayParseException を投げる
     * - 残高不足やカード無効などの業務エラーは GatewayDeclinedException を投げる
     */
    PaymentResult charge(PaymentRequest request)
            throws IOException, GatewayParseException, GatewayDeclinedException;
}
