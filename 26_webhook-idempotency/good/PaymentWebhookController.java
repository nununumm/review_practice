package com.example.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 決済プロバイダからの Webhook を受け取る"受け口"。
 * 責務は「①署名検証 ②本文の解釈 ③Serviceへ委譲 ④正しいHTTPステータスを返す」に絞る。
 */
@RestController
@RequestMapping("/webhook")
public class PaymentWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final WebhookSignatureVerifier signatureVerifier;
    private final PaymentWebhookService paymentWebhookService;
    private final ObjectMapper objectMapper; // JSON文字列 ⇔ Javaオブジェクト の変換係

    public PaymentWebhookController(WebhookSignatureVerifier signatureVerifier,
                                    PaymentWebhookService paymentWebhookService,
                                    ObjectMapper objectMapper) {
        this.signatureVerifier = signatureVerifier;
        this.paymentWebhookService = paymentWebhookService;
        this.objectMapper = objectMapper;
    }

    /**
     * 【重要】本文は byte[]（生バイト列）で受け取る。
     * 署名(HMAC)は"送られてきた本文そのもの"に対して計算されるため、
     * 一度オブジェクトにパースしてから作り直すと1バイトでもズレて検証に失敗しうる。
     * だから「まず生のまま署名検証 → その後パース」の順にする。
     */
    @PostMapping("/payment")
    public ResponseEntity<String> handle(@RequestBody byte[] rawBody,
                                         @RequestHeader(value = "X-Signature", required = false) String signature) {
        // ── ① 門番：署名を検証。偽物・改ざんは即 401 で拒否し、業務処理には一切進ませない ──
        if (!signatureVerifier.isValid(rawBody, signature)) {
            logger.warn("Webhook署名の検証に失敗しました。リクエストを拒否します。"); // 署名やPIIは出さない
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        }

        // ── ② 本文を型付きDTOへ変換 ──
        final PaymentWebhookRequest request;
        try {
            request = objectMapper.readValue(rawBody, PaymentWebhookRequest.class);
        } catch (Exception e) {
            // 本文が壊れている＝プロバイダに再送させても直らない。400で「送り直し不要」を伝える。
            logger.warn("Webhook本文の解析に失敗しました", e);
            return ResponseEntity.badRequest().body("invalid payload");
        }

        // 扱うイベント種別以外は、受領だけして 200 を返す（再送させない）
        if (!"payment.succeeded".equals(request.type())) { // 定数を左に置きNPEを避ける
            return ResponseEntity.ok("ignored");
        }

        // ── ③ 業務処理は Service へ委譲。④正しいステータスを返す ──
        try {
            boolean processed = paymentWebhookService.handlePaymentSucceeded(request);
            // 処理済み(重複)でも 200 を返すのが正解。プロバイダに「もう再送不要」と伝えるため。
            return ResponseEntity.ok(processed ? "processed" : "duplicate");
        } catch (OrderNotFoundException e) {
            // データがおかしい系。再送されても直らないので 400（再送不要）。
            logger.warn("対象注文が見つかりません orderId={}", e.getOrderId());
            return ResponseEntity.badRequest().body("order not found");
        } catch (Exception e) {
            // 想定外エラー(DB一時障害など)は握りつぶさず 500 を返す。
            // → プロバイダが再送してくれる＝あとで自動的にリトライされる。
            //   bad/ のように失敗しても "OK"(2xx) を返すと、再送が止まり通知が永久に失われる。
            logger.error("Webhook処理中に想定外のエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
