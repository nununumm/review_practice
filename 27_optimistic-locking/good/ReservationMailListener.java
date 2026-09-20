package com.example.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 予約確定メールを送る担当。
 *
 * ★ポイント1：@TransactionalEventListener(phase = AFTER_COMMIT)
 *   「トランザクションが本当にコミットされた後」にだけ呼ばれる。
 *   bad/ ではコミット前（下書き段階）でメールを送っていたため、
 *   後でロールバックすると「予約は無いのにメールだけ届く」事故が起きた。
 *   AFTER_COMMIT にすることで「確定してから送る」を保証できる。
 *
 * ★ポイント2：@Async
 *   メール送信（＝外部への通信で遅い）を別スレッドに逃がす。
 *   予約のレスポンスをメール送信の完了まで待たせない。
 *   （利用には設定クラスに @EnableAsync が必要）
 */
@Component
@RequiredArgsConstructor
public class ReservationMailListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        mailService.sendReservationMail(event.userId(), event.seatId());
    }
}
