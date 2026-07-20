package com.example.auth;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ここが「テスト容易性」改善の“ごほうび”。
 * bad版では本物のSMTPに繋ぎ、時刻も毎回動いて検証できなかったが、
 * この設計なら Spring を起動せず、new で組み立てて秒で検証できる。
 */
class PasswordResetServiceTest {

    @Test
    void requestReset_トークンと有効期限を保存し案内メールを送る() {
        // --- 準備（すべて“偽物”を外から渡す）---

        // ① 時計を「2026-07-20 10:00:00 UTC」で固定する（Clock.fixed）。もう時刻は動かない！
        Instant 固定された今 = Instant.parse("2026-07-20T10:00:00Z");
        Clock 止まった時計 = Clock.fixed(固定された今, ZoneOffset.UTC);

        // ② トークンは常に同じ値を返す偽物にする（値を照合できる）
        TokenGenerator tokenGenerator = () -> "fixed-token-123";

        // ③ リポジトリとメール送信はモック（＝動いたフリをする偽物）
        PasswordResetTokenRepository repository = mock(PasswordResetTokenRepository.class);
        MailSender mailSender = mock(MailSender.class);

        // ④ 有効期限は設定クラスで「1時間」に（テストなら1分でもよい。ここは本番同等に）
        PasswordResetProperties properties = new PasswordResetProperties();
        properties.setExpiration(Duration.ofHours(1));

        PasswordResetService service =
                new PasswordResetService(repository, mailSender, 止まった時計, tokenGenerator, properties);

        // --- 実行 ---
        service.requestReset("user@example.com");

        // --- 検証 ---

        // (A) 保存されたエンティティを“横取り”して中身を確認する
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(repository).save(captor.capture());
        PasswordResetToken saved = captor.getValue();

        // トークンが正しく保存されているか
        assertThat(saved.getToken()).isEqualTo("fixed-token-123");
        // 有効期限は「固定した今 + 1時間」ちょうどか？ 時刻を固定したので“ピッタリ”検証できる
        assertThat(saved.getExpiresAt()).isEqualTo(固定された今.plus(Duration.ofHours(1)));

        // (B) 案内メールを、正しい宛先・本文（トークン入り）で送ろうとしたか
        verify(mailSender).send(
                eq("user@example.com"),
                eq("パスワード再設定のご案内"),
                contains("fixed-token-123"));
    }
}
