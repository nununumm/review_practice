package com.example.cart;

import java.util.Date;
import java.util.List;

/**
 * ユーザーの買い物カゴを表すクラス。
 * カゴに入っている商品名のリストと、カゴを作成した日時を持つ。
 */
public class ShoppingCart {

    private List<String> items;   // カゴの中の商品名の一覧
    private Date createdAt;       // カゴを作成した日時

    public ShoppingCart(List<String> items, Date createdAt) {
        this.items = items;         // 受け取ったリストを保持する
        this.createdAt = createdAt; // 受け取った日時を保持する
    }

    // カゴの中身を返す
    public List<String> getItems() {
        return items;
    }

    // カゴの中身を差し替える
    public void setItems(List<String> items) {
        this.items = items;
    }

    // カゴを作成した日時を返す
    public Date getCreatedAt() {
        return createdAt;
    }

    // カゴを作成した日時を差し替える
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
