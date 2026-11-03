import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderSummaryService {

    // 高額注文とみなすしきい値（円）。マジックナンバーを名前付き定数にして意図を明確にする
    private static final long HIGH_VALUE_THRESHOLD = 10_000L;

    /**
     * 注文一覧・明細一覧・商品マスタを受け取り、集計結果をまとめて返す。
     * どのケースでも null は返さず、必ず OrderSummary を返す（呼び出し側のNPEを防ぐ）。
     */
    public OrderSummary summarize(List<Order> orders, List<OrderItem> items, List<Product> products) {

        // 1. 商品マスタを「ID → 商品」の Map に一度だけ変換しておく。
        //    こうすると各明細の商品検索が Map.get で一発（O(1)）になり、
        //    明細ごとに products を線形探索する二重ループ（O(n²)）を避けられる。
        Map<Long, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 2. (a) 合計金額：各明細の「単価 × 数量」を出し、mapToLong().sum() で合算する。
        //    外側の可変変数に forEach で足し込む書き方をやめると、意図がそのまま式に出る。
        long total = items.stream()
                .mapToLong(item -> lineAmount(item, productById))
                .sum();

        // 3. (b) カテゴリ別売上：groupingBy でカテゴリごとにまとめ、
        //    summingLong で各グループの金額を合計する（手製 HashMap の containsKey/get/put が不要になる）。
        Map<String, Long> salesByCategory = items.stream()
                .collect(Collectors.groupingBy(
                        item -> productOf(item, productById).getCategory(), // 分類キー＝カテゴリ
                        Collectors.summingLong(item -> lineAmount(item, productById)))); // 各グループの合計

        // 4. (c) 高額注文の抽出：filter → collect(toList)。
        //    「条件に合う要素だけ集めて新しいリストを作る」という Stream 本来の使い方。
        //    該当なしのときも空リストになり、null は返らない。
        List<Order> highValueOrders = orders.stream()
                .filter(order -> order.getTotalAmount() >= HIGH_VALUE_THRESHOLD)
                .collect(Collectors.toList());

        // 5. 代表商品名：明細が空なら空文字にする（items.get(0) の IndexOutOfBounds を避ける）。
        //    Optional は get() で即取り出さず、map + orElse で「無いときの値」まで明示する。
        String topProductName = items.stream()
                .findFirst()
                .map(item -> productOf(item, productById).getName())
                .orElse("");

        return new OrderSummary(total, salesByCategory, highValueOrders, topProductName);
    }

    /** 1明細の金額（単価 × 数量）を計算する。商品検索はここに集約する */
    private long lineAmount(OrderItem item, Map<Long, Product> productById) {
        Product product = productOf(item, productById);
        return product.getPrice() * item.getQuantity();
    }

    /**
     * 明細の productId から商品を引く。見つからない場合は null を返さず、
     * 意味のある例外を投げる（データ不整合を黙って握りつぶさない）。
     */
    private Product productOf(OrderItem item, Map<Long, Product> productById) {
        Product product = productById.get(item.getProductId());
        if (product == null) {
            throw new IllegalStateException("商品マスタに存在しない productId です: " + item.getProductId());
        }
        return product;
    }
}
