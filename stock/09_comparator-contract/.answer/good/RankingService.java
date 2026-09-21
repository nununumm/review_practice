import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingService {

    /**
     * 並べ替えルール（Comparator）を1か所にまとめて定義しておく。
     *
     * 優先順位は「スコア降順 → 売上数降順 → 名前昇順（null は最後）」。
     * ポイントは3つ：
     *  1. comparingInt / comparingLong を使い、値の「引き算」をしない
     *     → int や long の引き算はオーバーフロー（＝表現できる範囲を超えて符号が反転する）で
     *       大小が逆転することがあるため。Integer.compare / Long.compare を内部で使う
     *       comparingInt / comparingLong なら、その事故が起きない。
     *  2. reversed() と thenComparing() を組み合わせて多段の並べ替えを組み立てる
     *     → 自前の if 分岐だと「片方向しか符号を返さない」等の書き間違いで
     *       比較の約束（反対称・推移律）を壊し、"Comparison method violates its
     *       general contract!" で落ちることがある。標準APIの組み立てなら約束が守られる。
     *  3. nullsLast で name が null のときも安全に扱う（NullPointerException を防ぐ）。
     */
    private static final Comparator<Product> RANKING_ORDER =
            Comparator.comparingInt(Product::getScore).reversed()                    // スコア降順
                    .thenComparing(Comparator.comparingLong(Product::getSalesCount).reversed()) // 売上数降順
                    .thenComparing(Product::getName,
                            Comparator.nullsLast(Comparator.naturalOrder()));        // 名前昇順・null は末尾へ

    /**
     * 商品リストを並べ替えたランキングを返す。
     * 引数のリストを直接書き換えず、コピーを作って並べ替える（＝呼び出し側の元リストを壊さない）。
     */
    public List<Product> rank(List<Product> products) {
        // new ArrayList<>(products) で中身をコピーした新しいリストを作る
        List<Product> ranked = new ArrayList<>(products);
        // List#sort に、上で定義した安全な並べ替えルールを渡す
        ranked.sort(RANKING_ORDER);
        return ranked;
    }
}
