package com.example.payment;

/**
 * 注文の状態を表す列挙型（enum）。
 *
 * 【なぜ enum にするのか】
 * bad/ では order.setStatus("PAID") のように "文字列" で状態を持っていた。
 * 文字列だと "PAID" / "Paid" / "PAYED"(スペルミス) など何でも入ってしまい、
 * タイプミスしてもコンパイル時に気づけない（実行して初めて事故る）。
 * enum にすると「取り得る状態はこの4つだけ」とコンパイラが保証してくれる。
 */
public enum OrderStatus {
    PENDING,   // 支払い待ち（注文はできたが入金前）
    PAID,      // 支払い済み
    CANCELED,  // キャンセル済み
    REFUNDED   // 返金済み
}
