# 第27問：イベントの座席予約サービス

**📅 出題日**: 2026年9月19日

## 📋 レビュー課題

コンサートやセミナーの座席をオンラインで予約する `SeatReservationService` です。

- `reserve(eventId, userId)`: そのイベントの空席をシステムが1つ選んで予約する
- `reserveSeat(seatId, userId)`: ユーザーが座席表から選んだ特定の座席を予約する

人気イベントでは、発売開始と同時に**大勢のユーザーが同時に**予約ボタンを押します。

対象: `bad/SeatReservationService.java`（および参考として `bad/Seat.java`）

「どこが問題か、どう直すべきか」をレビューしてみてください。

---

## ❌ NGポイント (bad/ 配下)

1. **二重予約＝lost update（更新の消失）〔本問の主役 / 並行処理〕**:
   「空席を読む → RESERVED に書き換える」という *read-modify-write* を、同時実行制御なしで行っている。
   AとBが同時に `reserve` を呼ぶと、二人とも「5番は AVAILABLE」と読み、二人とも「5番を RESERVED に」と書く。
   結果 **同じ席が2人に予約される**。後から書いた方が勝ち、先の予約は上書きで消える。人気イベントで確実に発生する。

2. **`Seat` エンティティに `@Version` が無い〔並行処理〕**:
   楽観的ロックの土台が無いので、JPA が「読んだ後に他人が更新した」ことを検知できない。1 の直接原因。

3. **`@Transactional` 欠如で部分コミット〔トランザクション/設計〕**:
   「座席の更新」と「予約レコードの作成」が別トランザクションになりうる。片方成功・片方失敗で
   **座席は RESERVED なのに予約レコードが無い**（またはその逆）という壊れた状態が残る。

4. **取り消せない副作用（確認メール）を確定前に送っている〔設計〕**:
   `save` の直後・コミット前にメールを送信。この後にロールバックすると
   **DBに予約が無いのにメールだけ客に届く**。メールは取り消せない。

5. **`reserve` は空席ゼロ時に NPE〔例外処理〕**:
   空席が無いと `target` が null のまま `target.setStatus(...)` で NullPointerException。原因の分かりにくい落ち方をする。

6. **`reserveSeat` の存在チェック欠如（`Optional.get()`）〔例外処理〕**:
   `findById(seatId).get()` は座席が無いと `NoSuchElementException`。`orElseThrow` で意図の伝わる例外にすべき。

7. **`reserveSeat` の状態チェック欠如〔設計〕**:
   すでに RESERVED の座席でも無条件で上書き予約できる＝**他人の予約を奪える**。

8. **状態がマジックストリング〔型安全性〕**:
   `"AVAILABLE"` / `"RESERVED"` は typo してもコンパイルが通り、実行時までバグに気づけない。網羅性も保証できない。

9. **全件ロードしてアプリ側 for で空席探索〔パフォーマンス〕**:
   `findByEventId` で全座席をメモリに載せてから for で探す。DB の `WHERE` / `LIMIT` を使えばよく、大規模イベントでスケールしない。

10. **例外の握りつぶし〔例外処理〕**:
    `catch (Exception e) { e.printStackTrace(); return "予約に失敗しました"; }`。
    原因が失われ、失敗がクライアントに正しく伝わらない（HTTPステータスも常に成功扱いになりがち）。

11. **バリデーション欠如〔設計〕**: `eventId` / `userId` / `seatId` の null チェック等が無い。

12. **文字列を戻り値にしている〔設計〕**:
    `"予約が完了しました（座席ID: 5）"` という画面用の文章を返しており、後続処理で座席IDを使うのが困難。構造化した DTO を返すべき。

## ⭕️ 改善案 (good/ 配下)

1. **楽観的ロック**: `Seat` に `@Version` を追加。同時更新は `OptimisticLockException` で弾き、二重予約を構造的に防ぐ（`reserve`）。
2. **悲観的ロック**: 席指定のように争奪が激しい経路は `@Lock(PESSIMISTIC_WRITE)`（`SELECT ... FOR UPDATE`）で読む瞬間に施錠し、他スレッドを待たせる（`reserveSeat`）。
3. **`@Transactional`** で座席更新と予約レコード作成を1トランザクションに束ね、部分コミットを防ぐ。
4. **メールはコミット後に送る**: `@TransactionalEventListener(AFTER_COMMIT)` ＋ `@Async` で「確定してから・非同期で」送る。
5. **状態は enum**（`SeatStatus`）で保持し、`@Enumerated(EnumType.STRING)` で永続化。
6. **状態遷移ルールを Seat 自身に**（`seat.reserve(userId)`）。「AVAILABLE のときだけ予約可」を1か所で守り、他人の予約上書きを防ぐ。
7. **空席検索は DB で絞り込み＋1件だけ取得**（`findAvailableSeats` ＋ `PageRequest.of(0,1)`）。空席ゼロは `NoAvailableSeatException`。
8. **存在チェックは `orElseThrow`**。意図の伝わる独自例外（`SeatNotFoundException` 等）に。
9. **例外は握りつぶさず伝播**させ、`@RestControllerAdvice` 等で 409 Conflict 等に対応づける。必要なら楽観ロック衝突はリトライ。
10. **戻り値は DTO**（`ReservationResult`）で構造化する。

## 💬 レビューコメント例文

> 座席予約おつかれさまです！ロジックはきれいに書けています。一点だけ、発売開始直後の同時アクセスを想定すると気になる箇所があります。
> いまの `reserve` は「空席を読む→RESERVEDに書く」の間に他の人が割り込めるので、人気公演だと同じ席が2人に予約されてしまう（lost update）可能性があります。
> 対策として、`Seat` に `@Version` を足して**楽観的ロック**で弾くのが定番です。席指定の `reserveSeat` のように争奪が激しい経路は `SELECT ... FOR UPDATE` の**悲観的ロック**の方が素直かもしれません。
> あわせて、`reserve` と `reserveSeat` に `@Transactional` を付けて「座席更新」と「予約レコード作成」を1つのトランザクションにまとめておくと、途中失敗時の中途半端な状態を防げます。
> それと確認メールは、いまコミット前に送っているので、後でロールバックすると予約が無いのにメールだけ届いてしまいます。`@TransactionalEventListener(AFTER_COMMIT)` で「確定後に送る」に寄せると安全です。
> このあたりはハマると原因究明が本当に大変なので、先回りで潰しておきましょう。困ったら一緒に見ます！
