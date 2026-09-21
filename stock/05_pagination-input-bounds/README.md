# 【ストック】商品一覧API

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

商品一覧を返す REST API です。クエリパラメータで `page`（ページ番号）と
`size`（1ページあたりの件数）を受け取り、該当ページの商品リストと
総件数をまとめて返します。データ取得は `ProductRepository` に任せています。

対象: `bad/ProductListController.java`（あわせて `bad/ProductListService.java`）
「どこが問題か、どう直すべきか」をレビューしてみてください。
