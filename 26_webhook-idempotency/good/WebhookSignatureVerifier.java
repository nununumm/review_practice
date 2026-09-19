package com.example.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;                       // HMAC（＝秘密キー入りハッシュ）を計算する道具
import javax.crypto.spec.SecretKeySpec;        // 秘密キーを Mac に渡すための入れ物
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;            // 比較を「タイミング攻撃」に強い形で行うために使う
import java.util.HexFormat;                    // バイト列を16進文字列に変換する（Java 17+）

/**
 * Webhook の署名（signature）を検証する専門クラス。
 *
 * 【役割】
 * 「届いた通知が、本当に決済プロバイダから来たものか？」を確かめる"門番"。
 * 秘密キーは決済プロバイダとこちらの2者だけが知っている合言葉。
 * 攻撃者は秘密キーを知らないので、正しい署名を作れない＝偽の通知を弾ける。
 */
@Component  // Spring に管理させる部品にする（＝DIでき、テスト時に差し替えやすい）
public class WebhookSignatureVerifier {

    // 秘密キーはソースに直書きしない（第14問）。設定ファイル/環境変数から注入する。
    // application.yml 等の payment.webhook.secret を読み込む。
    private final String secret;

    public WebhookSignatureVerifier(@Value("${payment.webhook.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * 受け取った「本文の生バイト列」と「送られてきた署名」が一致するか検証する。
     *
     * @param rawBody           HTTPリクエストボディの"生の"バイト列（パースする前のそのまま）
     * @param receivedSignature X-Signature ヘッダーで送られてきた署名
     * @return 本物なら true、偽物・改ざんなら false
     */
    public boolean isValid(byte[] rawBody, String receivedSignature) {
        // 署名ヘッダーが無ければ問答無用で不正
        if (receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }

        // ① 自分の秘密キーで、届いた本文から署名を"自分でも"計算する
        String expected = computeHmacSha256(rawBody);

        // ② 送られてきた署名と、自分で計算した署名を突き合わせる。
        //    通常の equals ではなく MessageDigest.isEqual を使う理由：
        //    文字列比較は「何文字目で違ったか」で処理時間が変わり、
        //    それを手がかりに署名を推測される（タイミング攻撃）のを防ぐため、
        //    常に一定時間で比較してくれる isEqual を使う。
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 本文バイト列を、秘密キー付きの HMAC-SHA256 でハッシュ化し、16進文字列にして返す。
     * ※ 使うアルゴリズム(HMAC-SHA256)自体は公開情報。安全性は「秘密キー」だけが担保する。
     */
    private String computeHmacSha256(byte[] rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(rawBody);          // 本文＋秘密キーから"指紋"を計算
            return HexFormat.of().formatHex(hash);       // バイト列を "3a7f..." のような文字列に
        } catch (Exception e) {
            // 設定ミス（キー未設定など）は起動時に気づくべき想定外の事態なので、握りつぶさず例外に。
            throw new IllegalStateException("署名の計算に失敗しました", e);
        }
    }
}
