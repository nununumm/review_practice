package com.example.cart;

import org.springframework.stereotype.Service;  // Springに「これはサービス層の部品」と認識させる注釈

import java.time.Instant;                        // 現在時刻（不変の日時）を得るために使う

/**
 * 買い物カゴ（ShoppingCart）を組み立てたり操作したりするサービス。
 *
 * ポイントは、カートの中身へ触るときに
 * 「getItems().add(...)」のような“裏口”を一切使わず、
 * カートが公開している“正式な窓口”（addItem / markUsed）だけを呼ぶこと。
 */
@Service
public class CartService {

    /**
     * 新しい空のカートを作る。
     * 商品リストは渡さない（カートが自分で用意する）。
     */
    public ShoppingCart createCart() {
        return new ShoppingCart(Instant.now());  // 作成日時に「今」を渡してカートを生成
    }

    /**
     * カートに商品を追加する。
     * 内部リストを直接いじらず、カートの窓口 addItem を呼ぶ。
     * あわせて「使った」ので最終使用日時も更新する。
     */
    public void addItem(ShoppingCart cart, String item) {
        cart.addItem(item);   // 正式な窓口で追加（getItems().add(...) はもう使わない）
        cart.markUsed();      // 「使った」という操作を記録（日時の計算はカート側が担当）
    }

    /**
     * カートに入っている商品の数を返す。
     * getItems() は読み取り専用ビューだが、件数を数える（size）分には問題ない。
     */
    public int itemCount(ShoppingCart cart) {
        return cart.getItems().size();
    }
}
