package com.example.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 外部の決済ゲートウェイにカード決済を依頼するサービス。
 * 後から調査できるように、要所でログを残している。
 */
@Service
public class PaymentGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);

    @Autowired
    private PaymentGatewayClient client;

    public boolean charge(PaymentRequest request) {
        logger.info("決済開始 cardNumber=" + request.getCardNumber()
                + ", cvv=" + request.getCvv()
                + ", amount=" + request.getAmount()
                + ", token=" + request.getApiToken());

        try {
            PaymentResponse response = client.charge(request);
            logger.info("決済レスポンス: " + response);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("決済に失敗しました: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
