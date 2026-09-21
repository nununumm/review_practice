package good;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * キャンペーンの達成率計算と、割引額の按分（頭割り）を行うサービス。
 *
 * bad 版には「整数同士の割り算で小数が消える」「按分の端数が行方不明になる」という
 * お金の計算での典型的な事故が仕込まれていた。good 版では、
 *  - 率は割り算の順序を正し、小数を保てる形で計算する
 *  - 按分は割り切れない余り（端数）を配り切って、合計が元の額と必ず一致するようにする
 *  - 0除算や不正な入力を先にガードする
 * を守っている。
 */
@Service
public class CampaignCalcService {

    /**
     * 目標金額に対する達成率(%)を、小数第1位まで求める。
     *
     * bad 版は (int)(actual / target) * 100 だった。ここには2つの罠があった：
     *  1) actual / target は long 同士の割り算なので小数点以下が切り捨てられる（＝整数除算）。
     *     actual が target 未満だと商が 0 になり、達成率が常に 0% になる。
     *  2) 100 を掛けるのが割り算の「後」なので、0 に 100 を掛けてやはり 0 のまま。
     * → 先に 100 を掛ける／小数（BigDecimal）で割る、の順序と型が肝心。
     *
     * @param actual 現在の達成金額（0以上）
     * @param target 目標金額（1以上）
     * @return 達成率（%）。小数第1位まで（例: 66.7）
     */
    public BigDecimal achievementRate(long actual, long target) {
        // 0除算ガード：target が 0 だと割り算がエラー（ArithmeticException）になるので先に弾く
        if (target <= 0) {
            throw new IllegalArgumentException("目標金額は1以上である必要があります: " + target);
        }
        if (actual < 0) {
            throw new IllegalArgumentException("達成金額は0以上である必要があります: " + actual);
        }

        BigDecimal actualBd = BigDecimal.valueOf(actual);
        BigDecimal targetBd = BigDecimal.valueOf(target);

        // 先に 100 を掛けてから割る。BigDecimal.divide では割り算の桁数と丸め方を必ず指定する
        // （指定しないと割り切れないとき例外になる）。ここでは小数第1位まで四捨五入
        return actualBd.multiply(BigDecimal.valueOf(100))
                .divide(targetBd, 1, RoundingMode.HALF_UP);
    }

    /**
     * 合計割引額 total を count 個へ按分する。
     *
     * bad 版は total / count を全員に配っていたため、割り切れない余り（端数）が丸ごと消えていた。
     * 例：100円を3商品に配ると 100/3 = 33 円ずつ → 33×3 = 99 円で、1円が行方不明になる。
     * ここでは「基本額を全員に配り、余りを先頭から1円ずつ足す」ことで、
     * 配った合計が必ず元の total と一致するようにする。
     *
     * @param total 合計割引額（0以上）
     * @param count 対象商品の数（1以上）
     * @return 各商品への割引額のリスト（合計は必ず total と一致する）
     */
    public List<Long> distributeDiscount(long total, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("対象件数は1以上である必要があります: " + count);
        }
        if (total < 0) {
            throw new IllegalArgumentException("合計割引額は0以上である必要があります: " + total);
        }

        long base = total / count;       // 全員に等しく配る基本額（切り捨て）
        long remainder = total % count;  // 割り切れずに余った端数（＝配り切るべき残り円数）

        List<Long> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // 先頭から余りの分だけ +1 円して、端数を1円ずつ配り切る。
            // こうすると合計は base×count + remainder = total にぴったり一致する
            long amount = base + (i < remainder ? 1 : 0);
            result.add(amount);
        }
        return result;
    }
}
