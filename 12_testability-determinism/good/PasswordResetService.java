package com.example.auth;

import org.springframework.stereotype.Service;

import java.time.Clock;       // 「時計」を表す部品。System.currentTimeMillis() の代わりに“外から渡せる時刻源”
import java.time.Instant;     // 「ある一瞬の時刻」を表す型（Date の現代的な置き換え）

/**
 * パスワード再設定の申請を受け付けるサービス（模範解答版）。
 *
 * 【bad版からの主な改善】
 *   1. メール送信を `new SmtpMailSender(...)` で自前生成 → MailSender をコンストラクタで“外から受け取る”
 *      （テストで本物のメールを飛ばさず、モックに差し替え可能に）
 *   2. `System.currentTimeMillis()` 直接依存 → Clock をコンストラクタで受け取る
 *      （テストで「今」を固定でき、有効期限を正確に検証できる）
 *   3. トークンの UUID 直接生成 → TokenGenerator を注入（テストで値を固定できる）
 *   4. マジックナンバー(60*60*1000) → PasswordResetProperties の Duration に外部化
 *   5. @Autowired のフィールド注入 → コンストラクタ注入
 *      （必要な部品が一目で分かり、テストでは new で普通に組み立てられる）
 */
@Service   // このクラスがサービス層（ビジネスロジック担当）だとSpringに知らせる注釈
public class PasswordResetService {

    // すべて final ＝ 生成後は差し替わらない。必要な部品が「コンストラクタで出そろう」ことが保証される
    private final PasswordResetTokenRepository tokenRepository;
    private final MailSender mailSender;
    private final Clock clock;
    private final TokenGenerator tokenGenerator;
    private final PasswordResetProperties properties;

    /**
     * コンストラクタ注入。
     * Spring が起動時に、登録済みの部品（Bean）をここへ自動で差し込んでくれる。
     * テストでは、この引数に“偽物（モックや固定時計）”を渡して new するだけでよい。
     */
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                MailSender mailSender,
                                Clock clock,
                                TokenGenerator tokenGenerator,
                                PasswordResetProperties properties) {
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.clock = clock;
        this.tokenGenerator = tokenGenerator;
        this.properties = properties;
    }

    /**
     * 再設定を申請する：トークン発行 → 有効期限付きで保存 → 案内メール送信。
     */
    public void requestReset(String email) {
        // ① 再設定用トークンを発行（テストでは固定値にできる）
        String token = tokenGenerator.generate();

        // ② 「今」を Clock から取得する。ここが決定的（＝テストで固定可能）になった最大のポイント。
        //    有効期限 = 今 + 設定された期間。期間はマジックナンバーではなく Duration で外部化済み。
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.getExpiration());

        // ③ トークンを保存
        PasswordResetToken entity = new PasswordResetToken(email, token, expiresAt);
        tokenRepository.save(entity);

        // ④ 案内メールを送る。実体は“注入された”MailSender なので、テストではモックで検証できる。
        mailSender.send(email,
                "パスワード再設定のご案内",
                "以下のリンクから再設定してください: https://example.com/reset?token=" + token);
    }
}
