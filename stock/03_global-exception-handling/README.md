# 【ストック】商品・注文APIのエラー処理

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

ECサイトのバックエンドにある2つの REST API です。`ProductController` は商品を1件取得する API、
`OrderController` は注文を1件取得する API と注文を新規作成する API を持っています。
それぞれのエンドポイントで、Service が投げてくるエラー（存在しない商品／注文、入力不正、予期せぬ障害など）を
処理してレスポンスを返しています。

対象: `bad/ProductController.java`（あわせて `bad/OrderController.java` も見てください）
「どこが問題か、どう直すべきか」をレビューしてみてください。
