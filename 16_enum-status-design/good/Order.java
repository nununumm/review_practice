package com.example.order;

/**
 * 注文を表すエンティティ（＝DBの1行に対応するオブジェクト）。
 *
 * 【改善点】ステータスを String ではなく OrderStatus（enum）で持つ。
 *   → 「存在しない状態文字列」を setStatus に渡せなくなる（コンパイラが守ってくれる）。
 */
public class Order {

    private Long id;

    // ステータスの“型”を String → OrderStatus に変更。ここが今回の肝。
    private OrderStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
