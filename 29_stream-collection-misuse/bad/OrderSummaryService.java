import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderSummaryService {

    // 注文一覧・明細一覧・商品マスタを受け取り、集計結果をまとめて返す
    public OrderSummary summarize(List<Order> orders, List<OrderItem> items, List<Product> products) {

        // (a) 全明細の合計金額を出す
        long total = 0;
        items.forEach(item -> {
            // 明細の productId から、商品マスタを毎回まるごと探して単価を引く
            Product found = null;
            for (Product p : products) {
                if (p.getId().equals(item.getProductId())) {
                    found = p;
                }
            }
            total += found.getPrice() * item.getQuantity();
        });

        // (b) カテゴリ別の売上を積み上げる
        Map<String, Long> salesByCategory = new HashMap<>();
        items.forEach(item -> {
            Product found = null;
            for (Product p : products) {
                if (p.getId().equals(item.getProductId())) {
                    found = p;
                }
            }
            String category = found.getCategory();
            long amount = found.getPrice() * item.getQuantity();
            if (salesByCategory.containsKey(category)) {
                salesByCategory.put(category, salesByCategory.get(category) + amount);
            } else {
                salesByCategory.put(category, amount);
            }
        });

        // (c) 合計10000円以上の高額注文だけを抜き出す
        List<Order> highValueOrders = new ArrayList<>();
        orders.forEach(order -> {
            if (order.getTotalAmount() >= 10000) {
                highValueOrders.add(order);
            }
        });

        if (highValueOrders.isEmpty()) {
            return null;
        }

        // 代表商品（最初の明細の商品名）を参考情報として付ける
        Optional<Product> first = products.stream()
                .filter(p -> p.getId().equals(items.get(0).getProductId()))
                .findFirst();
        String topProductName = first.get().getName();

        return new OrderSummary(total, salesByCategory, highValueOrders, topProductName);
    }
}
