# 【ストック】JSON変換ユーティリティ

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

オブジェクトと JSON 文字列を相互変換する共通ユーティリティ `JsonConverter` です。
`toJson(obj)` でオブジェクトを JSON 文字列に、`fromJson(json, type)` で JSON 文字列を
オブジェクトに戻します。内部では Jackson の `ObjectMapper` を使っています。
この `JsonConverter` は、API のリクエスト/レスポンス処理やログ出力など、アプリ全体から
高頻度で呼ばれます。

対象: `bad/JsonConverter.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
