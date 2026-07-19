package com.example.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * パスワード再設定の申請を受け付けるサービス。
 * 再設定用のトークンを発行し、有効期限付きで保存して、ユーザーにメールを送る。
 */
@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public void requestReset(String email) {
        // 再設定用のトークンを発行する
        String token = UUID.randomUUID().toString();

        // 有効期限は今から1時間後
        Date expiresAt = new Date(System.currentTimeMillis() + 60 * 60 * 1000);

        // トークンを保存する
        PasswordResetToken entity = new PasswordResetToken(email, token, expiresAt);
        tokenRepository.save(entity);

        // ユーザーに再設定メールを送る
        SmtpMailSender mailSender = new SmtpMailSender("smtp.example.com", 587);
        mailSender.send(email, "パスワード再設定のご案内",
                "以下のリンクから再設定してください: https://example.com/reset?token=" + token);
    }
}
