package com.example.auth;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * MailSender の本番用実装（実際にSMTPでメールを送る係）。
 *
 * ★ユーザーの指摘「メール送信を @Async で非同期にする」について★
 *   本番の性能面では良い発想。メール送信（外部通信）は遅くなりがちなので、
 *   別スレッドに逃がして requestReset の応答を速くする、という狙いは妥当。
 *   ただし2点おさえておくこと。
 *     (1) 今回の主眼（テスト容易性）を解決するのは @Async ではなく「注入（DI）」の方。
 *         @Async を付けても、new で作り込んでいたら結局モックに差し替えられない。
 *         まず MailSender をインターフェースにして“外から渡す”のが先。
 *     (2) @Async は「別スレッドで動く」ため、テストで「送られたか」を確かめる時に
 *         タイミングがズレやすい（＝テストはむしろ書きにくくなる）。
 *         そこで、非同期にするのは “実装クラスのこのメソッド” に限定する。
 *         サービス層は MailSender インターフェース越しに呼ぶだけなので、
 *         テストでは同期的なモックを渡せて、非同期の影響を受けずに検証できる。
 *
 *   ＝「インターフェースで差し替え可能にする」→「実装の都合（非同期）はここに閉じ込める」
 *     という役割分担が、テスト容易性と本番性能を両立させるコツ。
 */
@Component   // Springの部品として登録（MailSender を必要とする場所へ自動注入される）
public class SmtpMailSender implements MailSender {

    private final PasswordResetProperties properties;   // SMTPホスト等の設定もマジックナンバー化しない

    public SmtpMailSender(PasswordResetProperties properties) {
        this.properties = properties;
    }

    @Async   // このメソッドを別スレッドで実行（呼び出し元をブロックしない）。@EnableAsync が必要。
    @Override
    public void send(String to, String subject, String body) {
        // 実際の送信処理（JavaMailSender など）をここに書く。
        // ホストやポートも properties から受け取り、bad版のような直書きにしない。
        // ...
    }
}
