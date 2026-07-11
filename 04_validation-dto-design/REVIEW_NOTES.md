# 第4問 復習ノート（会話で深掘りした論点まとめ）

> 2026-07-11 の対話で出た「?」を後で見返せるように整理したもの。
> コード本体は `bad/` `good/`、NGポイント一覧は `README.md` を参照。

---

## 1. エンティティ vs DTO（今回の核心）

- **エンティティ（`User`）＝ DBのテーブルの形**。`@Entity`付き。向く方向は **DB**。
- **DTO（`UserUpdateRequest` / `UserResponse`）＝ 通信で運ぶ箱**。向く方向は **クライアント（外）**。
- たとえ：**エンティティ＝社員名簿（給与・マイナンバーまで全部）／ DTO＝名刺（見せる項目だけ選ぶ）**。

### 「統一してはダメ?」への答え
統一したのが `bad/` の姿。実害は次の通り：

| 統一したせいで | 被害 |
|---|---|
| 出口で `User` を返した | password/role が **JSONで漏れる**（情報漏洩） |
| 入口で `User` を受けた | role を書き換えられ **管理者に昇格**（マスアサインメント） |
| DBの形＝APIの形 | DBに列を足すと **勝手にAPI仕様が変わる** |

→ **外の世界と接する境界では、面倒でもエンティティとDTOを分けるのが定石**。
　特に password/role のような「見せない・変えさせない」項目を持つ場合は必須。

---

## 2. DTOはいくつ作る?

- 「API 1本 = DTO 1個」ではない。**用途（入口/出口）ごとに作る**。1本のAPIで入口用+出口用の2個を持つことが多い。
- **形が同じなら使い回す**（例：複数のGETで同じ `UserResponse`）。
- **形が違えば分ける**（例：登録用は password 要る／更新用は要らない → 別Request）。
- 判断基準は毎回「このAPIが本当に必要とする項目は何か」。

---

## 3. ファイルの役割（混同しやすい）

| ファイル | 役割 |
|---|---|
| `ProfileController` | 受付。リクエストを受け、DTOで返す |
| `UserUpdateRequest` | 🔧入口DTO（name/emailだけ受け取る） |
| `UserResponse` | 📤出口DTO（id/name/emailだけ返す） |
| `UserService` | 業務ロジック本体 |
| `UserRepository` | DBへの読み書きの**道具**（findById/save）。JSONは無関係 |
| `User` | DBテーブルの形（裏方） |

※ `UserRepository` は「JSONを返す用」ではない、が要注意ポイントだった。

---

## 4. ビルダー / メソッドチェーン（`ResponseEntity` の書き方）

`return ResponseEntity.badRequest().body(errors);` の `.` 繋ぎの正体。

### タネ：メソッドが「次に呼べる相手」を return するから繋がる
- 普通のsetterは `void`（返さない）→ 繋げない。
- **戻り値を「自分自身」や「Builder」にすると繋げられる。**

### 種類A：自分自身を返す（fluent）
```java
public Pizza size(String s) { this.size = s; return this; } // ★return this
new Pizza().size("L").topping("チーズ");
```

### 種類B：専用Builderを持ち build() で締める（ResponseEntity, Lombok @Builder）
```java
User.builder().name("太郎").email("taro@ex.com").build();
```
- 組み立て中は本体を未完成/finalに保てる → より安全。

### ResponseEntity の中身（イメージ）
```java
ResponseEntity.badRequest()  // → BodyBuilder(脇役)が返る
    .body(errors);           // → 完成品 ResponseEntity が返る
```
- `ResponseEntity` = 「HTTPステータス + ボディ」をワンセットで包む箱。
- `.ok(x)` / `.status(HttpStatus.NOT_FOUND).body(x)` なども同じ組み立て。

### 見分け方：戻り値の型
| 戻り値 | 繋げる? |
|---|---|
| `void` | ❌ |
| 自分自身の型 | ✅ (fluent) |
| `Builder`等 | ✅ (build()で締める) |

### Spring Bootでの登場箇所
ResponseEntity / SecurityConfig の `http.〇〇().〇〇()` / Stream API / Optional /
Lombok `@Builder` / WebClient / MockMvc … **至る所で頻出**。

---

## 5. GlobalExceptionHandler の for文は Stream ではない

```java
for (FieldError error : ex.getBindingResult().getFieldErrors()) {
    errors.put(error.getField(), error.getDefaultMessage());
}
```
- これは **拡張for文（for-each）＝ただのループ**。Stream APIではない。
- 違反を1件ずつ「フィールド名 → メッセージ」でMapに詰めている。
- （参考）Streamなら `.stream().collect(Collectors.toMap(...))` で同じことが書ける。

---

## ひとことで持ち帰り
**「エンティティをそのままAPIの入口・出口に使うな。用途ごとのDTOを挟め。」**
`.` 繋ぎは「部品を足して最後に完成させる組み立て工法」。怖くない。
