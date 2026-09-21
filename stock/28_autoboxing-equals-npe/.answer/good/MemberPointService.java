package good;

import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 会員ポイント・ランクを判定するサービス。
 *
 * bad 版には「ラッパー型（＝Integer/Long/Boolean のようにオブジェクト版の数値・真偽値）」
 * ならではの罠が3つ仕込まれていた：
 *   ① Integer どうしの == 比較（参照比較になり、値が同じでも false になりうる）
 *   ② null になりうる Integer/Long を int/long に受けてアンボクシングで NPE
 *   ③ Boolean を if でそのまま使い、null だと同じく NPE
 * good 版ではこの3つを、値比較・null の明示的な扱いで直している。
 */
@Service
public class MemberPointService {

    // しきい値をマジックナンバーにせず定数化して意味を持たせる
    private static final long POINT_THRESHOLD = 1000L;

    private final MemberPointRepository repository;

    // コンストラクタで依存を受け取る（フィールドの @Autowired より、テスト時に差し替えやすい）
    public MemberPointService(MemberPointRepository repository) {
        this.repository = repository;
    }

    /**
     * 会員のランクIDがキャンペーン対象ランクと一致するか判定する。
     */
    public boolean isTargetRank(Integer rankId, Integer targetRankId) {
        // ★罠①対策：Integer どうしは == で比べてはいけない。
        //   == は「同じオブジェクト（実体）か」を見る参照比較で、
        //   Java は -128〜127 の Integer だけをキャッシュして使い回すため、
        //   その範囲は偶然 true になるが、128 以上になると同じ値でも false になる。
        //   値が等しいかは Objects.equals で比べる（両方 null でも安全に判定できる）。
        return Objects.equals(rankId, targetRankId);
    }

    /**
     * 会員の保有ポイント合計がしきい値(1000)以上か判定する。
     */
    public boolean hasEnoughPoints(Long memberId) {
        // ★罠②対策：SUM() は対象0件だと null を返しうる。
        //   これを int/long にそのまま代入すると、
        //   ラッパー→プリミティブへの自動変換（＝アンボクシング）時に null で NPE になる。
        //   Long のまま受け、null なら 0 とみなしてから比較する。
        Long total = repository.sumPoints(memberId);
        long safeTotal = (total != null) ? total : 0L;
        return safeTotal >= POINT_THRESHOLD;
    }

    /**
     * プレミアム会員かどうかを判定する。
     */
    public boolean isPremium(Long memberId) {
        // ★罠③対策：Boolean を if(flag) でそのまま使うと、
        //   null のとき boolean へのアンボクシングで NPE になる。
        //   Boolean.TRUE.equals(...) なら、flag が null でも false として安全に扱える。
        Boolean flag = repository.findPremiumFlag(memberId);
        return Boolean.TRUE.equals(flag);
    }
}
