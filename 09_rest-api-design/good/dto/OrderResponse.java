package com.example.shop.dto;

import com.example.shop.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 注文「返却」専用クラス（レスポンスDTO）。
// Entity（DB直結クラス）をそのまま返すと、見せたくない項目まで漏れる。
// 「外部に見せてよい項目だけ」をここに並べ、詰め替えてから返す。
public record OrderResponse(
        Long id,
        Long userId,
        Long productId,
        int quantity,
        BigDecimal totalPrice,
        LocalDateTime orderedAt
        // ↑ 原価・内部ステータス・他人の個人情報などは“あえて載せない”のがポイント。
) {

    // Entity → DTO へ詰め替える変換メソッド。
    // 「Entityのどの項目を外に出すか」をこの1か所に集約でき、漏れの防止になる。
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getOrderedAt()
        );
    }
}

/*
 * ============================================================================
 * 【参考】record を使わずに、昔ながらの「普通のクラス」で同じものを書くと…
 * ============================================================================
 * 上の record は、実は下記のクラスとほぼ同じもの。record は①フィールド／
 * ②コンストラクタ／③値の取り出しメソッド／④equals・hashCode・toString を
 * 自動で作ってくれるので、これだけの記述が「1行」に圧縮できている、というわけ。
 * （record は Java 16 以降で使える。古い環境ではこちらの書き方になる）

public final class OrderResponse {

    // ① フィールド（データの入れ物）。final = 後から書き換え不可（record も同じく不変）
    private final Long id;
    private final Long userId;
    private final Long productId;
    private final int quantity;
    private final BigDecimal totalPrice;
    private final LocalDateTime orderedAt;

    // ② コンストラクタ（値を受け取ってフィールドに詰める）
    public OrderResponse(Long id, Long userId, Long productId,
                         int quantity, BigDecimal totalPrice, LocalDateTime orderedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderedAt = orderedAt;
    }

    // ③ 値の取り出しメソッド。
    //    ※ record では get を付けず「id()」の形になるが、普通のクラスでは
    //      慣習的に「getId()」と書くことが多い（下の from() の呼び出しも getter 前提）。
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDateTime getOrderedAt() { return orderedAt; }

    // 変換メソッド（record 版と中身は同じ）
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getOrderedAt()
        );
    }

    // ④ 本来はここに equals() / hashCode() / toString() も手で書く必要がある
    //    （record ならこれらも全部自動。手書きは長く、書き忘れ・ミスの温床になる）
}
 * ============================================================================
 */
