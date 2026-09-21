# 【ストック】素のJDBCによるユーザー検索・更新

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

素の JDBC（＝Javaから直接DBを触る低レベルAPI）で書かれたユーザー管理のデータアクセスクラス（`UserDao`）です。
`JdbcTemplate` などの便利な道具を使わず、`Connection`（＝DBとの接続）・`PreparedStatement`（＝SQLの実行係）・`ResultSet`（＝検索結果の入れ物）を手で扱って、ユーザーの検索と表示名の更新を行っています。

対象: `bad/UserDao.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
