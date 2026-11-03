// 注文（1件の注文ヘッダ）
public class Order {
    private Long id;
    private long totalAmount; // この注文の合計金額（円）

    public Long getId() {
        return id;
    }

    public long getTotalAmount() {
        return totalAmount;
    }
}
