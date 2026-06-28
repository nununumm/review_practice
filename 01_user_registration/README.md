\# 第1問：ユーザー登録APIの設計とセキュリティ



\*\*📅 出題日\*\*: 2026年6月28日

\*\*URL\*\*: https://share.gemini.google/Sjr9X4Y5ztsM


\## ❌ NGポイント (bad/ 配下)

1\. \*\*パスワードの平文保存\*\*: DB漏洩時に致命的な被害が出る。

2\. \*\*Fat Controller\*\*: Controllerにビジネスロジック（DB操作など）を直接書いている。



\## ⭕️ 改善案 (good/ 配下)

1\. `BCryptPasswordEncoder` を使用してパスワードをハッシュ化する。

2\. 処理を `UserService` に委譲し、Controllerはリクエストの受け渡しに専念する。

3\. `SecurityConfig` (`@Configuration`) でBeanを登録し、Serviceで \*\*DI（依存性の注入）\*\* を利用して疎結合にする。

