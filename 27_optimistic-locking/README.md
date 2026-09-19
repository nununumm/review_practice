# 第27問：イベントの座席予約サービス

**📅 出題日**: 2026年9月19日

## 📋 レビュー課題

コンサートやセミナーの座席をオンラインで予約する `SeatReservationService` です。

- `reserve(eventId, userId)`: そのイベントの空席をシステムが1つ選んで予約する
- `reserveSeat(seatId, userId)`: ユーザーが座席表から選んだ特定の座席を予約する

人気イベントでは、発売開始と同時に**大勢のユーザーが同時に**予約ボタンを押します。

対象: `bad/SeatReservationService.java`（および参考として `bad/Seat.java`）

「どこが問題か、どう直すべきか」をレビューしてみてください。
