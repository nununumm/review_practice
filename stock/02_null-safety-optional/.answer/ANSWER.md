## ❌ NGポイント (bad/ 配下)

1. **見つからないとき `null` を返す設計（`findMember`）**: `findMember` は会員がいないと `null` を返す。すると呼び出し側は毎回「`null` だったらどうする？」というチェックを書かないといけない。1か所でもチェックを忘れると、その `null` に対して `.getStatus()` などを呼んだ瞬間に **NPE（＝ヌルポ。null の中身を触ろうとして落ちるエラー）** で落ちる。実際このコードでは `isActive` / `isPremium` などが `findMember` の戻り値をノーチェックで使っており、存在しない ID を渡すと即クラッシュする。「ない」という状態は `null` ではなく、**空の Optional（＝空っぽかもしれない箱）** か **例外** で表すべき。

2. **`member.getStatus().equals("ACTIVE")` の並び順（NPE の罠）**: 「変数側 `.equals(定数)`」の順で書くと、`status` が `null` のとき `null.equals(...)` になって NPE で落ちる。定数は絶対に `null` にならないので、**`"ACTIVE".equals(member.getStatus())`** と定数側から呼べば、中身が `null` でも落ちずに `false` になる。しかも今回は `status` が `null` になりうる（DB 未設定など）ことを一切考慮していない。

3. **ラッパ型（`Boolean` / `Integer`）を `==` で比較している**:
   - `member.getPremiumFlag() == true` は二重の地雷。①`Boolean`（オブジェクト）を `boolean`（値）と比べるため自動で“開封（アンボックス）”されるが、**フラグが `null` だとこの開封の瞬間に NPE で落ちる**。②そもそも真偽の判定に `== true` は不要。
   - `haveSameRank` の `m1.getRank() == m2.getRank()` は **`Integer` の“キャッシュの罠”**。Java は -128〜127 の `Integer` だけ使い回す（同じオブジェクト）ので `==` がたまたま合うが、**128 以上になると別オブジェクト扱いで `==` が `false` になる**。ランク程度なら小さいが、「小さい値ではテストが通り、大きい値で本番だけ落ちる」典型の気まぐれバグ。数の中身を比べたいなら `==` ではなく `.equals` / `Objects.equals`。
   - `isPremium` の `member.getRank() == 3` も、`rank` が `null` だと開封時に NPE。

4. **`Optional` をメソッドの引数にしている（`buildGreeting`）**: `Optional` は本来「**戻り値**で “ないかもしれない” を表す」ための道具。引数に使うと、呼ぶ側は値を渡すのに `Optional.of(...)` でわざわざ包む手間が増え、しかも `null` を渡すことも空 Optional を渡すこともできて状態が増えるだけ。引数は素直に `Member` を受け取り、「存在する会員」を渡してもらうのが筋。

5. **`Optional.get()` を存在チェックなしで呼ぶ（`getEmail` / `buildGreeting`）**: `findById(id).get()` は、会員がいないと `NoSuchElementException` で落ちる。`get()` は「中身が絶対ある」と確信できるとき以外は使わない。`orElseThrow` / `map` / `orElse` など、空のときの振る舞いをセットで書ける安全なメソッドを使う。Optional を使う意味は「空チェックを強制すること」なのに、`get()` で素通りしては台無し。

6. **空文字 `""` と `null` の混同（`getDisplayName`）**: `name != null` だけ見ているので、名前が **空文字 `""`（＝長さ0の文字）** だとチェックをすり抜け、画面に「名無し」の空白が表示されてしまう。実務では「未入力＝空文字」で保存されることも多い。`null` も `""` も「未設定」として一緒に弾く必要がある。

## ⭕️ 改善案 (good/ 配下)

1. **`null` を返さず Optional か例外にする**: 探すだけの `findMember` は `Optional<Member>` を返し、「いて当然」の場面用に `getMember` を用意して、いなければ `MemberNotFoundException`（独自例外）を投げる。呼び出し側は「空チェックを型で強制される」か「例外で確実に気づける」かのどちらかになり、NPE の握りつぶしが起きない。

2. **文字列比較は `"定数".equals(...)` の順**: `"ACTIVE".equals(member.getStatus())` にすれば、`status` が `null` でも落ちずに `false`。「変数.equals(定数) ではなく 定数.equals(変数)」を口ぐせにする。

3. **ラッパ型の比較を安全にする**:
   - `Boolean` は `Boolean.TRUE.equals(member.getPremiumFlag())`。`null` でも `false` になり、`== true` の冗長さも消える。
   - `Integer` の中身比較は `Objects.equals(m1.getRank(), m2.getRank())`。キャッシュの罠を踏まず、両方 `null` でも正しく `true`、片方だけ `null` でも安全に `false`。

4. **`Optional` は戻り値専用にする**: `buildGreeting(Member member)` のように引数は素の `Member` にする。「取得は呼ぶ側で済ませ、ここには存在する会員が渡ってくる」という契約をシンプルに保つ。

5. **`get()` を使わず安全に取り出す**: `getEmail` は `findById(id).map(Member::getEmail).orElseThrow(...)` のように、空のときの振る舞いをセットで書く。`orElseThrow` / `orElse` / `map` / `ifPresent` を使えば `isPresent` + `get` の2段構えすら要らない。

6. **空文字も未設定として弾く**: `StringUtils.hasText(member.getName())`（Spring 付属）を使うと、`null`・空文字・空白だけの文字をまとめて「中身なし」と判定できる。表示名のような「見せる文字」は必ずこれで守る。

## 💬 レビューコメント例文

> 会員照会まわり、処理の流れは分かりやすく書けています 🙆‍♂️ いくつか「null まわりで本番だけ落ちる」パターンが潜んでいるので、一緒に潰しておきたいです。
>
> まず `findMember` が見つからないとき `null` を返している点。呼ぶ側が毎回 null チェックを迫られて、`isActive` などが実際ノーチェックで `.getStatus()` を呼んでいるので、存在しない ID で NPE になります。「ない」は `null` ではなく `Optional`（探すだけの用途）か、いて当然の場面は `orElseThrow` で独自例外に、と分けると事故が減ります。あわせて `member.getStatus().equals("ACTIVE")` は `status` が null だと落ちるので、`"ACTIVE".equals(...)` と定数側から呼ぶ癖をつけると安全です。
>
> それと `getPremiumFlag() == true` はフラグが null のとき開封時に落ちるので `Boolean.TRUE.equals(...)` に、`getRank() == 別のrank` は Integer のキャッシュの都合で 128 以上だけ false になる地味な罠があるので `Objects.equals(...)` にしておきましょう。表示名は `""` が素通りしてしまうので `StringUtils.hasText(...)` でまとめて弾けます。最後に `Optional` は「戻り値で “ないかも” を表す」道具なので、`buildGreeting` の引数は素の `Member` に、`get()` は `map`/`orElseThrow` に置き換えると Optional 本来の“空チェック強制”が効いてきます。次のリファクタで一緒にやってみましょう 💪
