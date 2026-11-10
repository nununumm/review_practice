// 注文明細（どの商品を何個買ったか）
public class OrderItem {
    private Long productId; // 商品マスタを引くためのID
    private int quantity;   // 数量

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
