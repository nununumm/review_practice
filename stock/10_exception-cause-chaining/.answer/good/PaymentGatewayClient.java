package com.example.payment;

import java.io.IOException;

/**
 * 外部の決済ゲートウェイを叩く低レベルの部品。（bad/ と同じ）
 * この「生の例外」たちを、上のサービスが受け取って、意味のある独自例外に翻訳する。
 * - IOException           … 通信の失敗（一時的かもしれない＝システム系）
 * - GatewayParseException … レスポンスが壊れていた（＝システム系）
 * - GatewayDeclinedException … 残高不足など業務的に断られた（＝業務系）
 */
public interface PaymentGatewayClient {

    PaymentResult charge(PaymentRequest request)
            throws IOException, GatewayParseException, GatewayDeclinedException;
}
