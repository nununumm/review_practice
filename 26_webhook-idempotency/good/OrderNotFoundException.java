package com.example.payment;

/**
 * 対象の注文が見つからないときに投げる専用例外。
 *
 * 【なぜ専用の例外にするのか】
 * bad/ は findById(orderId).get() で、注文が無いと中身の分かりにくい
 * NoSuchElementException が飛んでいた。専用例外にすると
 *  - 呼び出し側で「注文が無いケース」だけを狙って捕まえられる
 *  - ログやレスポンスで原因が一目で分かる
 */
public class OrderNotFoundException extends RuntimeException {

    private final Long orderId;

    public OrderNotFoundException(Long orderId) {
        super("注文が見つかりません: orderId=" + orderId);
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
