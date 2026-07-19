package com.example.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 注文「作成」用の受け取り専用クラス（リクエストDTO）。
// URLに ?userId=1&quantity=2 とぶら下げる代わりに、JSON本文をこの形で受け取る。
// record = 「値を入れる箱」を短く書ける文法。中身は不変（作った後に書き換わらない）で安全。
public record CreateOrderRequest(

        // @NotNull = 未入力（null）を許さない。バリデーションが自動で弾いてくれる。
        @NotNull(message = "userId は必須です")
        Long userId,

        @NotNull(message = "productId は必須です")
        Long productId,

        // @Min(1) = 1個未満（0個やマイナス）の注文を弾く。おかしな入力は入り口で止める。
        @Min(value = 1, message = "quantity は1以上を指定してください")
        int quantity
) {
}
