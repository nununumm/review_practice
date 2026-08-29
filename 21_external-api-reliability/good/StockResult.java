package com.example.stock;

/**
 * 1商品ぶんの在庫確認結果を、呼び出し元（Controller）へ返すためのオブジェクト。
 *
 * record（＝値を持つだけの不変クラスを短く書く仕組み）で定義しているので、
 * productCode / status / availableQuantity の3つのフィールドと、
 * それらのgetter・equals・toString が自動生成される。一度作ったら中身は変わらない。
 */
public record StockResult(
        String productCode,        // どの商品か
        StockStatus status,        // 引当可能 / 在庫不足 / 確認失敗 のどれか
        int availableQuantity      // 実際に引き当て可能な在庫数（status が CHECK_FAILED のときは意味を持たない）
) {
}
