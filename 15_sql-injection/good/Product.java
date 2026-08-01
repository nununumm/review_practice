package com.example.shop.product;

// 検索結果の1行を入れる“器”（データを持つだけのクラス）。
public class Product {
    private Long id;
    private String name;
    private int price;
    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
