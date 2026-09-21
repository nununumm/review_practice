## ❌ NGポイント (bad/ 配下)

1. **引き算で大小比較していてオーバーフローする（`b.getScore() - a.getScore()`）**:
   Comparator（＝並べ替えルール）は「a が前なら負、後なら正、同じなら 0」を返す約束。
   `b.getScore() - a.getScore()` は普段は動きますが、`int` は約 -21億〜+21億までしか表せません。
   たとえば `score` が `2,000,000,000` と `-2,000,000,000` のとき、その差は約40億で `int` の範囲を
   はみ出し（＝オーバーフロー）、**符号が反転して大小が逆になります**。結果、ランキングが逆順に壊れます。
   `salesCount`（`long`）でも同じで、しかも `(int) salesDiff` と int に切り詰めているため、
   long の差が int 範囲を超えると値そのものが化けます。

2. **多段比較の組み立てが非対称・推移律違反（`rankBySalesFirst`）**:
   `if (a.getSalesCount() > b.getSalesCount()) return -1;` の後、それ以外は名前比較に落としています。
   これだと「a の売上が b より少ない」ときに逆方向（＋の値）を返さず、名前の結果を返してしまいます。
   その結果、`compare(a, b)` と `compare(b, a)` の符号が食い違ったり（＝反対称性の破れ）、
   「a<b かつ b<c なのに a>c」のような矛盾（＝推移律の破れ）が起きます。
   要素数が一定以上になると Java の `sort` がこの矛盾を検知し、
   **`IllegalArgumentException: Comparison method violates its general contract!`** で落ちます。
   （＝並べ替えルールが「守るべき約束」を破っている、という実行時エラー）

3. **`name` が null だと NullPointerException**:
   `a.getName().compareTo(b.getName())` は、`name` が null の商品が1つでも混ざると
   その場で NullPointerException（＝null に対してメソッドを呼んだエラー）になります。
   表示名が未設定の商品はいくらでもあり得るのに、その考慮がありません。

4. **`equals` と `compare` の整合が考えられていない**:
   `compare` が 0 を返す（＝並べ替え上は同順位）ことと、2つの商品が `equals` で等しいことは別物です。
   bad の `Product` は `equals` を定義しておらず、同順位の扱いも曖昧です。
   `TreeSet` / `TreeMap` など「compare が 0 の要素は同一とみなす」仕組みで使うと、
   別商品なのに片方が消える、といった事故につながります（この観点は意識だけでも持っておく）。

5. **並べ替えのキーがミュータブル（後から書き換え可能）**:
   `Product` は `setScore` などの setter を持ち、`score` や `salesCount` を後から変更できます。
   ソートの最中や後にこれらのキーが書き換わると、並び順が壊れたり、上の「contract 違反」を誘発します。
   さらに `rank` は受け取ったリストを `Collections.sort` で**その場で並べ替えて**いるため、
   呼び出し側が渡した元のリストの順序まで書き換えてしまいます（副作用）。

## ⭕️ 改善案 (good/ 配下)

1. **引き算をやめ、`comparingInt` / `comparingLong` と `Integer.compare` / `Long.compare` を使う**:
   これらは内部で安全な比較を行い、オーバーフローで符号が反転する事故が起きません。

2. **`reversed()` + `thenComparing()` で多段ソートを組み立てる**:
   `Comparator.comparingInt(Product::getScore).reversed()`（スコア降順）に、
   `.thenComparing(Comparator.comparingLong(Product::getSalesCount).reversed())`（売上降順）、
   `.thenComparing(Product::getName, ...)`（名前昇順）を連ねます。
   標準APIが反対称性・推移律を保証してくれるので、自前 if の書き間違いによる contract 違反を防げます。

3. **`Comparator.nullsLast(Comparator.naturalOrder())` で null を安全に扱う**:
   `name` が null の商品は末尾に回し、NullPointerException を防ぎます。

4. **`equals` を id で明示し、compare と equals の違いを意識する**:
   「順位が同じ（compare==0）」と「同じ商品（equals）」は別、という前提をコード上で明確にします。

5. **キーをイミュータブルにし、元リストを壊さない**:
   `Product` を final フィールド＋setter 無し（イミュータブル）にして、ソート中にキーが変わらないようにします。
   さらに `rank` は `new ArrayList<>(products)` でコピーしてから `sort` し、呼び出し側の元リストを保護します。

## 💬 レビューコメント例文

> ランキングの並べ替えロジック、確認しました。1点だけ実運用で怖いのが、`b.getScore() - a.getScore()`
> の引き算比較です。スコアが大きな正負の値になると int があふれて符号が逆転し、順位がひっくり返る
> ことがあります。ここは `Comparator.comparingInt(Product::getScore).reversed()` のように標準APIで
> 組み立てると、オーバーフローの心配なく「スコア降順→売上降順→名前昇順」を安全につなげられます。
> あわせて `rankBySalesFirst` は片方向しか符号を返していないので、データ量が増えると
> "Comparison method violates its general contract!" で落ちる可能性があります。名前が null のときの
> NPE対策も兼ねて `nullsLast` を入れておくと安心です。直し方も一緒に見てみましょう！
