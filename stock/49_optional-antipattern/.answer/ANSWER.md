## ❌ NGポイント (bad/ 配下)

1. **`isPresent()` ＋ `get()` の多用で、Optional を使う意味が無くなっている（核心・可読性）**: `if (member.isPresent()) { ... member.get() ... }` という書き方は、結局 `if (member != null) { member.foo() }` という昔ながらの null チェックと同じで、Optional の利点（値の有無を型で表し、チェック忘れを防ぐ）が消えている。しかも `member.get()` を2回呼んでおり冗長。`get()` は「中身が無いと `NoSuchElementException` で落ちる危険な取り出し」で、本来できるだけ使わないもの。`map` / `orElse` をつなげば同じことが1行で安全に書ける。

2. **`greeting()` がメソッド引数に `Optional` を取っている（アンチパターン・設計）**: 引数の Optional は避けるべき、というのが一般的な指針。理由は、`greeting(null)` と `greeting(Optional.empty())` の2通りの「無い」が生まれて呼び出し規約が曖昧になること、そして呼び出し側に毎回 Optional を作らせる負担をかけること。引数は「実体」で受け取り、「値があるかどうか」は呼び出し側で解決させるのが素直。

3. **`greeting()` の中で `get()` を存在チェックなしで呼んでおり落ちる（バグ）**: `member.get()` は Optional が空なら `NoSuchElementException`。引数 Optional と相まって、空を渡された瞬間に例外になる。

4. **`displayName()` の分岐が二重ネストで読みにくい＋ `"ゲスト"` リテラルが重複（可読性／低）**: null 判定のネストと、既定値 `"ゲスト"` の2か所へのべた書きは、`map(...).orElse(GUEST)` と名前付き定数で一掃できる。

## ⭕️ 改善案 (good/ 配下)

1. **`map` / `orElse` で宣言的に書く**: `findById(id).map(Member::getNickname).orElse(GUEST)` のように、「あれば変換、無ければ既定値」を1本の流れにする。`isPresent()`/`get()` の入れ子が消え、`get()` の危険も無くなる。

2. **引数に Optional を使わない**: `greeting(Member member)` のように実体で受け取る。「会員が確実にいること」は呼び出し側で保証する。

3. **`get()` を避ける**: どうしても取り出すなら `orElseThrow(...)` で「無いとき何が起きるか」を明示する。

4. **既定値を名前付き定数にする**: `"ゲスト"` を `GUEST` 定数にまとめ、重複を無くす。

## 💬 レビューコメント例文

> `findById` が Optional を返すのをちゃんと受けているのは good です。ただ、その Optional の使い方が「昔の null チェック」のままになっているのがもったいないですね。`isPresent()` で確認して `get()` で取り出す、を繰り返すのは、結局 `if (x != null)` と同じで、Optional にした意味がほとんど消えてしまうんです。`get()` は中身が無いと例外で落ちる危険な取り出しなので、できれば使いたくありません。ここは `findById(id).map(Member::getNickname).orElse("ゲスト")` と書けば、同じ動きが1行で、しかも安全に表現できます。

> もう1点、`greeting()` が引数に `Optional` を取っているのは避けたいパターンです。引数の Optional は「null かもしれない Optional」という二重の曖昧さを生みますし、中で `get()` しているので空を渡されると落ちます。引数は `Member` の実体で受け取って、「会員がいるかどうか」は呼ぶ側で解決してもらう形にしましょう。Optional は map / orElse をつないで「あれば〜、無ければ既定値」を宣言的に書く道具、と捉えると一気に読みやすくなりますよ。
