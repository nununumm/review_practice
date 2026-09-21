# 【ストック】注文明細の合計金額を集計するサービス

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

注文の明細行（`OrderLine`：単価 `unitPrice` と数量 `quantity` を持つ）のリストから、
合計金額を計算する `OrderTotalCalculator` です。`calculateTotal()` で全明細の
「単価 × 数量」を足し合わせ、`averagePerItem()` で「合計金額 ÷ 総数量」の
1点あたり平均単価を返します。BtoB 向けで、1注文に大量・高額の明細が並ぶこともあります。

対象: `bad/OrderTotalCalculator.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
