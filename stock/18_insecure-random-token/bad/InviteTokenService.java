import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class InviteTokenService {

    // 招待トークンを組み立てるのに使う文字の一覧
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    // トークンの長さ
    private static final int TOKEN_LENGTH = 8;

    private final InviteRepository inviteRepository;

    public InviteTokenService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    /**
     * チームへの招待トークンを発行して保存し、そのトークン文字列を返す。
     */
    public String issueToken(Long teamId, String invitedEmail) {

        // 現在時刻をもとに乱数の元（シード）を決める
        Random random = new Random(System.currentTimeMillis());

        // 現在時刻をトークンの先頭に埋め込む
        long now = System.currentTimeMillis();
        StringBuilder token = new StringBuilder(Long.toString(now, 36));

        // 残りの桁をランダムな文字で埋める
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = random.nextInt(CHARS.length());
            token.append(CHARS.charAt(index));
        }

        String tokenValue = token.toString();

        // 発行したトークンを保存する
        Invite invite = new Invite();
        invite.setTeamId(teamId);
        invite.setInvitedEmail(invitedEmail);
        invite.setToken(tokenValue);
        inviteRepository.save(invite);

        // 発行結果をログに残す
        System.out.println("招待トークンを発行しました: teamId=" + teamId
                + ", email=" + invitedEmail + ", token=" + tokenValue);

        return tokenValue;
    }
}
