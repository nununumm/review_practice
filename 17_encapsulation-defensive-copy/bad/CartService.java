package com.example.cart;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 買い物カゴ（ShoppingCart）を組み立てたり、
 * カゴに商品を追加したりするサービス。
 */
@Service
public class CartService {

    /**
     * 商品名のリストから新しいカゴを作る。
     */
    public ShoppingCart createCart(List<String> items) {
        return new ShoppingCart(items, new Date());
    }

    /**
     * カゴに商品を1つ追加する。
     */
    public void addItem(ShoppingCart cart, String item) {
        cart.getItems().add(item);
    }

    /**
     * カゴに入っている商品の数を返す。
     */
    public int itemCount(ShoppingCart cart) {
        return cart.getItems().size();
    }
}
