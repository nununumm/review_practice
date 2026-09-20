package com.example.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatReservationService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    // メールサービスを直接呼ばず、「予約が確定した」というイベントを発行するだけにする（後述）
    private final ApplicationEventPublisher eventPublisher;

    /**
     * イベントの空席を1つ確保して予約する。
     *
     * @Transactional を付けることで「座席の更新」と「予約レコードの作成」を
     * 1つのかたまり（トランザクション）にする。途中で失敗したら両方まとめて無かったことに
     * なる（＝座席だけRESERVEDで予約レコードが無い、という壊れた状態が起きない）。
     */
    @Transactional
    public ReservationResult reserve(Long eventId, Long userId) {
        validate(eventId, userId);

        // 空席だけをDBで絞り込み、先頭の1件だけ取得する（全件ロードしない）
        List<Seat> availableSeats = seatRepository.findAvailableSeats(eventId, PageRequest.of(0, 1));
        if (availableSeats.isEmpty()) {
            // 空席が無いことを「例外」で明確に伝える（bad/ は target が null のまま NPE だった）
            throw new NoAvailableSeatException(eventId);
        }
        Seat target = availableSeats.get(0);

        // 状態遷移のルール（AVAILABLEのときだけ予約可）は Seat 自身が持っている
        target.reserve(userId);

        // ※明示的な save は必須ではない（管理下のエンティティはトランザクション終了時に自動反映される）。
        //   ここで @Version が効き、他人が先に更新していれば ObjectOptimisticLockingFailureException が飛ぶ。

        Reservation reservation = createReservation(eventId, target.getId(), userId);

        // メールは「今すぐ」送らず、コミットが確定した後に送る（下の Listener 参照）
        eventPublisher.publishEvent(new ReservationConfirmedEvent(userId, target.getId()));

        return new ReservationResult(reservation.getId(), target.getId());
    }

    /**
     * 座席を指定して予約する（座席表から選ぶケース）。
     *
     * 争奪が激しい席指定なので、ここでは悲観的ロック（FOR UPDATE）で「読んだ瞬間に施錠」する。
     * こうすると同じ席を狙う他スレッドはコミットまで待たされ、二重予約が構造的に起きない。
     */
    @Transactional
    public ReservationResult reserveSeat(Long seatId, Long userId) {
        validate(seatId, userId);

        // .get() ではなく orElseThrow で「無ければ意図の伝わる例外」にする
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));

        // 「AVAILABLEのときだけ予約」のルールは Seat.reserve() 内でチェックされる
        seat.reserve(userId);

        Reservation reservation = createReservation(seat.getEventId(), seatId, userId);

        eventPublisher.publishEvent(new ReservationConfirmedEvent(userId, seatId));

        return new ReservationResult(reservation.getId(), seatId);
    }

    /**
     * 予約レコードの組み立て＆保存を1か所にまとめる（2メソッドで重複していた処理の共通化）。
     */
    private Reservation createReservation(Long eventId, Long seatId, Long userId) {
        Reservation reservation = new Reservation();
        reservation.setEventId(eventId);
        reservation.setSeatId(seatId);
        reservation.setUserId(userId);
        return reservationRepository.save(reservation);
    }

    /**
     * 入力チェック。null や不正値は「業務処理に入る前」に弾く（防御的プログラミング）。
     */
    private void validate(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("必須パラメータが指定されていません");
        }
    }

    // 補足：例外は bad/ のように握りつぶして "失敗しました" と返さない。
    // OptimisticLockException / SeatAlreadyReservedException などはそのまま上位（@RestControllerAdvice等）へ
    // 伝播させ、HTTP 409(Conflict) 等の適切なステータスで「別の席をどうぞ」とクライアントに知らせる。
    // 楽観ロックの衝突（ObjectOptimisticLockingFailureException）は、必要ならここで捕まえて
    // 数回リトライする実装にしてもよい。
}
