package com.example.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 決済プロバイダから届く Webhook 本文（JSON）を受け取るための DTO。
 *
 * 【なぜ Map<String,Object> をやめて DTO にするのか】
 * bad/ では payload.get("amount") のたびに (int) へ手動キャストしていた。
 * これは「型が合っている保証がなく、値が無ければ実行時に落ちる」危険な書き方。
 * DTO（＝受け渡し専用のデータの箱）にすると、
 *  - 期待するフィールドと型が1か所に明文化される
 *  - Jackson(JSON→Javaの変換ライブラリ)が型変換を安全にやってくれる
 *  - どんなデータが来る想定かがコードを読むだけで分かる
 *
 * @JsonIgnoreProperties(ignoreUnknown = true):
 *   プロバイダが将来フィールドを増やしても、知らない項目は無視して壊れないようにする。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookRequest(
        String eventId,    // このイベント固有のID。再送されても同じ値＝冪等性の"鍵"になる
        String type,       // 例: "payment.succeeded"
        Long orderId,      // 対象の注文ID
        int amount         // 入金額（円・整数）
) {
}
