# 【ストック】会員の表示名を組み立てるサービス

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

会員情報から画面に出す表示名を組み立てる `MemberQueryService` です。
`displayName()` は会員 ID からニックネームを返し（ニックネーム未設定や会員が
いなければ "ゲスト"）、`greeting()` は会員を受け取ってあいさつ文を作ります。
リポジトリの `findById` は `Optional<Member>` を返す前提で、Optional を使って
書かれています。

対象: `bad/MemberQueryService.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
