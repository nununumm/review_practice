// 商品マスタ（商品1件の情報）
public class Product {
    private Long id;
    private String name;
    private String category; // カテゴリ（例：本、食品、家電）
    private long price;      // 単価（円）

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getPrice() {
        return price;
    }
}
