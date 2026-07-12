package com.example.demo.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder; // パスワード照合を安全に行う道具
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    // クラスごとに1つロガーを持つ（定番の書き方）
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository userRepository;
    // DBには平文パスワードではなく「ハッシュ化した文字列」を保存しておき、照合はこれ経由で行う
    private final PasswordEncoder passwordEncoder;

    // コンストラクタで依存を受け取る（DI = 依存性注入）。テスト時にモックへ差し替えやすい
    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean login(String email, String rawPassword) {
        // ★① パスワードは絶対にログに出さない。
        // ★② メールは個人情報(PII)なのでマスキングして出す。
        // ★⑤ "+" 連結ではなく "{}"(プレースホルダ)を使う（性能＆ログ改ざん対策）。
        logger.info("ログイン試行を受け付けました: email={}", maskEmail(email));

        User user = userRepository.findByEmail(email);

        // ★④ 「ユーザーがいない」も「パスワード違い」も、外から区別できないよう同じ扱いにする（ユーザー列挙対策）。
        //     どちらのケースも、内部的には同じ「ログイン失敗」として処理する。
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            // ★③ ユーザーの打ち間違いは"業務エラー"であって"システム障害"ではない。
            //     error ではなく info/warn 程度に留め、本物の障害ログを埋もれさせない。
            //     ★存在有無を断定する文言（"ユーザーが存在しません"等）はログにも残さない。
            logger.info("ログインに失敗しました: email={}", maskEmail(email));
            return false;
        }

        // ★③ 成功時も user オブジェクトを丸ごと出さない。
        //     必要な識別子（IDなど）だけを明示的に選んで出す。将来項目が増えても勝手に漏れない。
        logger.info("ログインに成功しました: userId={}", user.getId());
        return true;
    }

    /**
     * メールアドレスを調査可能な最小限までマスキングする。
     * 例: "taro.yamada@example.com" -> "ta***@example.com"
     * ローカル部（@より前）の先頭2文字だけ残し、あとは伏せる。
     */
    private String maskEmail(String email) {
        if (email == null) {
            return "(null)";
        }
        int at = email.indexOf('@');
        // "@" が無い、またはローカル部が短すぎて隠す意味がある場合は、全部伏せる
        if (at <= 2) {
            return "***";
        }
        // 先頭2文字 + "***" + "@以降のドメイン"
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
