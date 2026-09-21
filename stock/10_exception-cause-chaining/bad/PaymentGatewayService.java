package com.example.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * 外部の決済ゲートウェイ呼び出しをラップするサービス。
 * 上位（Controller など）はこのサービスを呼ぶだけで決済できる、という想定。
 */
@Service
public class PaymentGatewayService {

    private static final Logger log = Logger.getLogger(PaymentGatewayService.class.getName());

    @Autowired
    private PaymentGatewayClient client;

    /**
     * 決済を実行する。失敗したら決済できなかったことを呼び出し側に伝える。
     */
    public PaymentResult pay(PaymentRequest request) {
        try {
            return client.charge(request);
        } catch (Exception e) {
            throw new RuntimeException("決済に失敗しました");
        }
    }

    /**
     * 決済を試みて、取引IDを返す。取れなければ null を返す。
     */
    public String tryPayAndGetTransactionId(PaymentRequest request) {
        try {
            PaymentResult result = client.charge(request);
            return result.getTransactionId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 決済が承認されたかどうかだけを返す簡易メソッド。
     */
    public boolean isPaymentApproved(PaymentRequest request) throws Exception {
        boolean approved = false;
        try {
            PaymentResult result = client.charge(request);
            approved = result.isApproved();
            return approved;
        } catch (IOException e) {
            log.severe("決済で通信エラー: " + e.getMessage());
            throw e;
        } finally {
            return approved;
        }
    }
}
