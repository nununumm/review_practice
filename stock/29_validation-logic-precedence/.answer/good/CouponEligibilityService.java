package good;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * クーポンを適用できるかどうかを判定するサービス。
 *
 * bad 版では「複雑な条件を一つの巨大な if にまとめて」いたため、
 * 演算子の優先順位・境界（以上/超）・否定の書き方がすべて壊れていた。
 * good 版では、条件を「意味のある名前を持った小さな判定メソッド」に分解し、
 * 括弧で優先順位を明示し、早期 return で読みやすくしている。
 */
@Service
public class CouponEligibilityService {

    // マジックナンバーを名前付き定数にする（「5000って何の数字？」を無くす）
    private static final int MIN_AMOUNT = 5000; // クーポン適用に必要な最低購入金額

    /**
     * クーポンを適用できるかを判定する。
     *
     * @param member       会員（null は「異常」なので早期に弾く）
     * @param amount       購入金額
     * @param campaignStart キャンペーン開始日（この日を含む）
     * @param campaignEnd   キャンペーン終了日（この日を含む）
     * @return 適用できるなら true
     */
    public boolean canUseCoupon(Member member, int amount,
                                LocalDate campaignStart, LocalDate campaignEnd) {
        // null を黙って NullPointerException で落とさず、はっきり異常として弾く
        if (member == null) {
            throw new IllegalArgumentException("会員情報が指定されていません");
        }

        // 条件を「名前のついた真偽値」に分解する。名前を読めば意図が分かる（コメント不要になる）
        boolean rankEligible = isRankEligible(member.getRank(), amount);
        boolean withinCampaign = isWithinCampaign(LocalDate.now(), campaignStart, campaignEnd);
        boolean loginOk = isLoggedInAndActive(member);
        boolean withinLimit = member.isCouponEnabled() && amount <= member.getCouponLimit();

        // 最後は分解した条件を && でつなぐだけ。全体像がひと目で読める
        return rankEligible && withinCampaign && loginOk && withinLimit;
    }

    /**
     * ランクと金額の条件。
     * 「GOLD または SILVER」で「かつ 5000円以上」──括弧で優先順位を必ず明示する。
     * bad 版の `GOLD || SILVER && amount >= 5000` は、&& が || より先に評価されるため
     * `GOLD || (SILVER && amount >= 5000)` と解釈され、「GOLDなら金額に関係なく常にtrue」だった。
     */
    private boolean isRankEligible(Rank rank, int amount) {
        boolean targetRank = (rank == Rank.GOLD || rank == Rank.SILVER); // まずランク条件だけを括弧でまとめる
        return targetRank && amount >= MIN_AMOUNT;                       // そのうえで金額条件と && でつなぐ
    }

    /**
     * 今日がキャンペーン期間内か（開始日・終了日を「含む」）。
     * 「start以上 かつ end以下」を、境界を取り違えないように書く。
     * ・!today.isBefore(start) は「today >= start（開始日当日を含む）」
     * ・!today.isAfter(end)    は「today <= end（終了日当日を含む）」
     * bad 版は isAfter(start) で開始日を除外し、end.plusDays(1) で終了翌日まで通す二重の境界ミスだった。
     */
    private boolean isWithinCampaign(LocalDate today, LocalDate start, LocalDate end) {
        return !today.isBefore(start) && !today.isAfter(end);
    }

    /**
     * ログイン済み「かつ」退会していない会員か。
     * 「ログインしていて、なおかつ有効」の両方が真のときだけ true。
     * bad 版は「弾く条件」を !loggedIn && !active（＝両方falseのときだけ弾く）と書いており、
     * ド・モルガンの法則を取り違えていた。
     * 正しくは !(loggedIn && active) = (!loggedIn || !active)。ここでは肯定形で素直に書く。
     */
    private boolean isLoggedInAndActive(Member member) {
        return member.isLoggedIn() && member.isActive();
    }
}
