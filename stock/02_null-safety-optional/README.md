# 【ストック】会員情報照会サービス

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

会員（Member）を ID で照会し、会員が「有効（ACTIVE）」かどうか、プレミアム会員かどうか、画面に出す表示名は何か、といった判定をまとめて行うサービスです。会員データの取得は `MemberRepository`（`findById` が結果を包んで返す想定）を通して行います。

対象: `bad/MemberService.java` / `bad/Member.java` / `bad/MemberRepository.java`

「どこが問題か、どう直すべきか」をレビューしてみてください。
