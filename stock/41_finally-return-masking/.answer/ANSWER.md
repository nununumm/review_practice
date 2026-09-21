## ❌ NGポイント (bad/ 配下)

1. **`finally` の中に `return ""` があり、本体の戻り値も例外も握りつぶしている（核心）**: `finally`（＝try/catch を抜ける直前に必ず通る「後片付け専用の部屋」）の中で `return "";` してしまっている。Java は finally の return を最優先するため、try で `props.getProperty("api.key")` が返そうとした値も、catch が返そうとした `null` も、途中で発生した例外も、**すべて `""` で上書きされて消える**。つまりこのメソッドは、成功しようが失敗しようが、いつも空文字を返す。呼び出し側は「キーが空だっただけ」なのか「読み込みに失敗した」のかを一生区別できず、しかもエラーはどこにも伝わらない。最悪の握りつぶしパターン。

2. **`try-with-resources` を使わず手動 close していて、finally が入れ子地獄になっている**: `BufferedReader` を `finally` の中で `close()` しているが、close 自体も `IOException` を投げうるので、その周りをさらに try/catch で囲む羽目になり、読みづらいうえに NG 1 の温床（finally での return）を生んでいる。Java 7 以降は `try ( ... )` の括弧内でリソースを開けば、正常でも例外でも自動的に閉じてくれる。

3. **`catch (Exception e)` が広すぎ＋`printStackTrace()`＋`return null` で原因が消える**: あらゆる例外を一括で捕まえ、標準エラーに出すだけ（ログ基盤にも乗らない）で `null` を返している。呼び出し側は `null` を受け取り、それを使った瞬間に別の場所で NPE を起こす。「本当は設定ファイルが無かった」という本当の原因は、`null` になった時点で失われている。

4. **戻り値が `null` / `""` / 実際の値の3種類あり、呼び出し側が状態を判別できない**: 成功で実キー、catch で `null`、finally で `""`。同じメソッドが3通りの意味の違う値を返すため、呼び出し側は防ぎようがない。

5. **ファイルパスのハードコードと文字コード未指定（低）**: `"/opt/app/config.properties"` を直書きしており環境ごとに差し替えられずテストもできない。`FileReader` は OS 依存の文字コードで開くため、環境によって文字化けするリスクもある。

## ⭕️ 改善案 (good/ 配下)

1. **`try-with-resources` に置き換え、`finally` を自分で書かない**: リソースの close はコンパイラに任せる。finally が消えれば「finally の return で握りつぶす」事故は構造的に起きなくなる。

2. **`finally` に `return` を書かない**（大原則）: finally は後片付けだけに使い、値を返すのは try / catch の中だけにする。

3. **失敗は原因付きで投げ直す**: `catch (IOException e)` で握りつぶさず、`new UncheckedIOException("...", e)` のように**元の例外 e を第2引数に渡して**包んで投げる。こうするとスタックトレースに根本原因が残り、呼び出し側も失敗に気づける。

4. **「見つからない」を曖昧な値でごまかさない**: `api.key` が無ければ空文字ではなく `IllegalStateException` で明示的に知らせる。

5. **パスは外から注入・文字コードは明示**: `Path` をコンストラクタで受け取り、`StandardCharsets.UTF_8` を指定して開く。

## 💬 レビューコメント例文

> リソースを finally できちんと閉じようとしている意識はすごく良いです。その上で、この finally には見つけにくい重大な罠が潜んでいるので直しましょう。`finally` の中に `return "";` がありますよね。実は Java の finally の return は最強で、try が返そうとした本物のキーも、catch の null も、途中で起きた例外も、全部この `""` で上書きして消してしまうんです。結果、このメソッドは成功しても失敗してもいつも空文字を返していて、呼ぶ側は失敗に気づけません。

> 直し方はシンプルで、まず `BufferedReader` の手動 close をやめて `try (BufferedReader reader = ...)` の try-with-resources にしましょう。これで close は自動になり、そもそも finally を書かなくて済みます。finally に return を書かない、は鉄則として覚えておいてください。あわせて `catch (Exception)` で null を返すのもやめて、`throw new UncheckedIOException("...", e)` のように**元の例外 e を包んで投げ直す**と、原因がスタックトレースに残って原因調査がぐっと楽になります。パスも直書きせず外から渡すようにすれば、テストもしやすくなりますよ。
