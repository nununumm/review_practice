import org.slf4j.Logger;           // ログ出力の窓口（System.out.println の代わりに使う）
import org.slf4j.LoggerFactory;    // Logger を作るための入り口
import org.springframework.stereotype.Service;

import java.security.SecureRandom; // 暗号レベルの安全な乱数（＝予測されにくいランダム値）を作るクラス
import java.time.Instant;          // 「今この瞬間の時刻」を表すクラス
import java.util.Base64;           // バイト列を文字列に変換する（ここではURLで安全な形式を使う）

@Service
public class InviteTokenService {

    // このクラス専用のログ出力係。出力先や形式を後からまとめて制御できる。
    private static final Logger log = LoggerFactory.getLogger(InviteTokenService.class);

    // トークンの「元ネタ」となるランダムなバイト数。
    // 32バイト = 256ビット分のランダムさがあり、総当たり（＝片っ端から試す攻撃）では現実的に当てられない。
    private static final int TOKEN_BYTES = 32;

    // 招待トークンの有効期限（＝いつまで使えるか）。ここでは発行から3日間とする。
    private static final long EXPIRE_DAYS = 3;

    // トークンが衝突（＝偶然同じ値になること）したときに、何回まで作り直すか。
    private static final int MAX_RETRY = 5;

    // SecureRandom は使い回してよい（毎回 new すると逆に無駄）。
    // java.util.Random と違い、時刻などから次の値を予測することができない。
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final InviteRepository inviteRepository;

    public InviteTokenService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    /**
     * チームへの招待トークンを発行して保存し、そのトークン文字列を返す。
     * 予測されにくく・十分に長く・期限付き・一意（＝重複しない）なトークンを作る。
     */
    public String issueToken(Long teamId, String invitedEmail) {

        // 衝突しても作り直せるように、生成〜保存を繰り返しの中で行う。
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {

            // 1. ランダムなバイト列を用意する（＝トークンの素）。
            //    SecureRandom を使うことで、過去の値から次の値を推測されない。
            byte[] randomBytes = new byte[TOKEN_BYTES];
            SECURE_RANDOM.nextBytes(randomBytes);

            // 2. バイト列を「URLセーフなBase64」で文字列にする。
            //    URLに入れても壊れない文字だけを使い、末尾の余分な "=" は付けない。
            String tokenValue = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            // 3. すでに同じトークンが存在しないか確認する（＝一意性チェック）。
            //    万一かぶっていたら、作り直す（次のループへ）。
            if (inviteRepository.existsByToken(tokenValue)) {
                continue;
            }

            // 4. 招待情報を組み立てて保存する。有効期限もここで付ける。
            Invite invite = new Invite();
            invite.setTeamId(teamId);
            invite.setInvitedEmail(invitedEmail);
            invite.setToken(tokenValue);
            invite.setExpiresAt(Instant.now().plusSeconds(EXPIRE_DAYS * 24 * 60 * 60)); // 今から3日後
            inviteRepository.save(invite);

            // 5. ログにはトークンそのものを出さない（＝漏れたら悪用される機密情報のため）。
            //    「誰宛に発行したか」だけ分かれば運用上は十分。
            log.info("招待トークンを発行しました: teamId={}, email={}", teamId, invitedEmail);

            return tokenValue;
        }

        // ここまで来た＝何度作り直しても衝突した。異常事態なので黙って握りつぶさず例外にする。
        throw new IllegalStateException("招待トークンの発行に失敗しました（衝突が続きました）");
    }
}
