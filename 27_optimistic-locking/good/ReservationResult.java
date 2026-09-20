package com.example.reservation;

/**
 * 予約結果を呼び出し側に返すためのDTO（データの入れ物）。
 *
 * bad/ は "予約が完了しました（座席ID: 5）" という「文章」を返していた。
 * 文章は画面表示用であって、プログラムが後続処理で使うには扱いにくい
 * （文字列を切り出して座席IDを取り出す、なんてことになる）。
 * 必要な値を構造化して返す方が、テストもしやすく再利用しやすい。
 */
public record ReservationResult(Long reservationId, Long seatId) {
}
