package com.example.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * このアプリで使う「部品（Bean）」の登録場所。
 *
 * ここで登録した部品を、Spring が PasswordResetService のコンストラクタへ自動で差し込んでくれる。
 */
@Configuration   // このクラスが「Beanの登録場所」であることを示す注釈
@EnableConfigurationProperties(PasswordResetProperties.class)   // 設定値クラスを有効化して注入できるようにする
public class AppConfig {

    /**
     * ★今回のキモ★
     * Spring は Clock（時計）を標準では用意してくれないので、ここで“本番用の時計”を登録する。
     *
     * bad版の `System.currentTimeMillis()` は「今、何時か」を直接コードに焼き込んでいた。
     * それをやめ、「時刻は Clock という部品から受け取る」形にする。
     *   ・本番   … ここで登録する“実際に時を刻む時計”を使う
     *   ・テスト … 「2026-07-20 10:00:00 で止まった時計」を作って渡す → 時刻を固定できる！
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();   // システム既定タイムゾーンで現在時刻を刻む本番用の時計
    }

    /**
     * トークン発行の本番用実装を部品として登録。
     * テストでは、この Bean の代わりに“固定値を返す偽物”を渡せる。
     */
    @Bean
    public TokenGenerator tokenGenerator() {
        return new TokenGenerator.UuidTokenGenerator();
    }
}
