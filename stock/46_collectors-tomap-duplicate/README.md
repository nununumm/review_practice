# 【ストック】商品コードで引ける索引を作るサービス

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

商品（`Product`：商品コード `code` を持つ）のリストを、「商品コード → 商品」の
`Map` に変換して素早く引けるようにする `ProductIndexService` です。
`indexByCode()` で Stream の `Collectors.toMap` を使って索引を作り、
`findByCode()` でその索引から商品コードを大文字化して検索します。
商品リストは外部システムから取り込んだデータで、内容は事前に保証されていません。

対象: `bad/ProductIndexService.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
