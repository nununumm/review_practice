package com.example.reservation;

/**
 * 「予約が確定した」という出来事（イベント）を表す小さな入れ物。
 *
 * サービスはメールを直接送らず、この出来事を publish する（＝知らせるだけ）。
 * 「実際にメールを送る人」は別（下の Listener）に任せる。こうすると、
 * サービスは業務ロジックに集中でき、通知手段（メール/SMS/プッシュ）を後から差し替えやすい。
 */
public record ReservationConfirmedEvent(Long userId, Long seatId) {
}
