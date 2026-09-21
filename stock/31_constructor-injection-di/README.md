# 【ストック】会員通知サービスの依存の受け取り方

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

会員へお知らせメールを送る `NotificationService`（Spring の `@Service`）です。
会員情報を引くリポジトリ、メールを送る送信部品、送信履歴を残すリポジトリの3つに依存しており、
`notifyMember(Long memberId, String message)` で「会員を探す → メールを送る → 履歴を保存する」を行います。
Spring の DI（＝必要な部品を外から渡してもらう仕組み）を使って、これらの依存を受け取っています。

対象: `bad/NotificationService.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
