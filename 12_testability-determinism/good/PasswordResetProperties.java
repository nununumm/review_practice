package com.example.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * パスワード再設定まわりの「設定値」をまとめて持つクラス。
 *
 * ★なぜ別クラスに切り出す？★
 *   bad版では有効期限が「60 * 60 * 1000」というマジックナンバー（＝意味の分からない数字）で、
 *   コードに直接埋め込まれていた。これだと
 *     ・何の数字か分かりにくい
 *     ・テストのとき「1分」に短くしたくても、コードを書き換えるしかない
 *   という2つの困りごとがある。
 *
 *   そこで「設定値」だけを外に追い出し、application.yml などから注入できるようにする。
 *   これがユーザーの言っていた「有効期限を別クラスに設定してDIで注入」の正体。
 *
 * ★使い方（application.yml のイメージ）★
 *   password-reset:
 *     expiration: 1h      # 本番は1時間
 *   テストのときは expiration: 1m のように、設定ファイルや @TestPropertySource で差し替えられる。
 */
@ConfigurationProperties(prefix = "password-reset")   // "password-reset.〇〇" の設定値をこのクラスに流し込む注釈
public class PasswordResetProperties {

    /**
     * 再設定トークンの有効期間。
     * Duration 型にしておくと「1h」「30m」のような表記をそのまま受け取れて、
     * 「ミリ秒に直すと…」という計算（＝マジックナンバーの温床）が不要になる。
     */
    private Duration expiration = Duration.ofHours(1);   // 設定が無いときの既定値は1時間

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }
}
