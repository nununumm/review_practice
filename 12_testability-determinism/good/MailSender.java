package com.example.auth;

/**
 * 「メールを送る」という“役割”だけを定義した窓口（インターフェース）。
 *
 * ★なぜインターフェースにする？★
 *   bad版では、サービスの中で直接 `new SmtpMailSender("smtp.example.com", 587)` していた。
 *   これだと、テストのたびに本物のSMTPサーバー（＝メール配達所）へ本当に送りにいってしまう。
 *   しかも `new` で内部に作り込んでいるので、「送ったフリ（＝モック）」に差し替える隙間が無い。
 *
 *   そこで「メールを送る」という“役割”だけを先に約束事（インターフェース）として決めておく。
 *   ・本番 … この役割を実装した SmtpMailSender を差し込む
 *   ・テスト … この役割を実装した“偽物”を差し込む（Mockito のモックなど）
 *   こうすれば、テストで実際にメールを飛ばさずに「send が呼ばれたか」を検証できる。
 */
public interface MailSender {

    /**
     * メールを1通送る。
     * @param to      宛先メールアドレス
     * @param subject 件名
     * @param body    本文
     */
    void send(String to, String subject, String body);
}
