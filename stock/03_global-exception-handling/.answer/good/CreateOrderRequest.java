package com.example.shop.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 注文作成APIが受け取る入力を表すクラス（リクエスト用DTO）。
 *
 * なぜ Map<String,Object> ではなくこれを使う？
 * ・Map で受けると「productId が入っているか」「数量が数字か」を毎回自分で確認せねばならず、
 *   その確認漏れがバグや例外の温床になる。
 * ・専用クラス＋バリデーション注釈にすれば、Spring が"入口"で自動チェックしてくれる。
 *   型変換の失敗も Spring が 400 に整えてくれるので、Controller が薄くなる。
 */
public class CreateOrderRequest {

    @NotNull // null を許さない（未指定ならこの時点で弾く）
    private Long productId;

    @NotNull
    @Min(1) // 1以上でなければならない（0以下の数量を入口で拒否）
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
