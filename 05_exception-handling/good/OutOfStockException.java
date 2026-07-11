package com.example.shop.product;

/**
 * 「在庫が足りず購入できない」ことを表す専用の例外クラス。
 *
 * ポイント：
 * ・「在庫不足」はバグではなく "業務上ありうる想定内の状況"。
 *   だからこそ雑な RuntimeException ではなく、意味の分かる専用の型で表現する。
 * ・後述の GlobalExceptionHandler でこの型を 409(Conflict) に割り当てる。
 */
public class OutOfStockException extends RuntimeException {

    // 「在庫が足りません」という固定メッセージを親に渡す。
    // ※ 現在の在庫数など "内部の細かい情報" はレスポンスに載せない（情報を出しすぎない）。
    public OutOfStockException() {
        super("在庫が不足しているため購入できません。");
    }
}
