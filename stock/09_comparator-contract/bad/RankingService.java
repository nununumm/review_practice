import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingService {

    // 商品リストを「スコア降順 → 売上降順 → 名前昇順」で並べ替えてランキングを作る
    public List<Product> rank(List<Product> products) {

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                // まずスコアの降順（大きいほど上位）
                int scoreDiff = b.getScore() - a.getScore();
                if (scoreDiff != 0) {
                    return scoreDiff;
                }
                // スコアが同じなら売上数の降順
                long salesDiff = b.getSalesCount() - a.getSalesCount();
                if (salesDiff != 0) {
                    return (int) salesDiff;
                }
                // それ以外は名前の昇順
                return a.getName().compareTo(b.getName());
            }
        });

        return products;
    }

    // スコアが同点のときに、売上が多い方を優先して並べ替えるバリエーション
    public List<Product> rankBySalesFirst(List<Product> products) {

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                // 売上が多い方を上位にしたい
                if (a.getSalesCount() > b.getSalesCount()) {
                    return -1;
                }
                // 売上が同じか少ないときは名前で並べる
                return a.getName().compareTo(b.getName());
            }
        });

        return products;
    }
}
