import java.util.List;
import java.util.Map;

// 集計結果をまとめて返すための入れ物
public class OrderSummary {
    private final long totalAmount;                 // 全明細の合計金額
    private final Map<String, Long> salesByCategory; // カテゴリ別の売上
    private final List<Order> highValueOrders;       // 高額注文の一覧
    private final String topProductName;             // 参考：代表商品名

    public OrderSummary(long totalAmount, Map<String, Long> salesByCategory,
                        List<Order> highValueOrders, String topProductName) {
        this.totalAmount = totalAmount;
        this.salesByCategory = salesByCategory;
        this.highValueOrders = highValueOrders;
        this.topProductName = topProductName;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Long> getSalesByCategory() {
        return salesByCategory;
    }

    public List<Order> getHighValueOrders() {
        return highValueOrders;
    }

    public String getTopProductName() {
        return topProductName;
    }
}
