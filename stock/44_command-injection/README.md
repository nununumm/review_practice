# 【ストック】アップロードPDFのサムネイル生成サービス

**📅 出題日**: （ストック作成）

## 📋 レビュー課題

ユーザーがアップロードした PDF から、外部のコマンドラインツール（`pdftoppm`）を呼び出して
サムネイル画像（PNG）を生成する `ThumbnailService` です。`createThumbnail(fileName)` で、
アップロード先のファイル名を受け取り、`Runtime.getRuntime().exec(...)` で変換コマンドを実行します。
`fileName` は画面からのアップロード時に決まる、ユーザー由来の値です。

対象: `bad/ThumbnailService.java`
「どこが問題か、どう直すべきか」をレビューしてみてください。
