package com.example.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
@Getter // getterは公開してよいが、setterは全部は公開しない（状態を勝手に書き換えられないように）
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String seatNumber;   // 例: "A-12"

    /**
     * ★この問題の主役★ 楽観的ロック用のバージョン番号。
     *
     * @Version を付けると、JPAが更新のたびにこの数値を +1 して見張る。
     * 「自分が読んだときのバージョン」と「更新しようとした瞬間のDBのバージョン」が
     * 食い違っていたら（＝その間に他人が先に更新していたら）、
     * OptimisticLockException を投げて弾いてくれる。
     * これが無いと、二人が同時に同じ空席を読んで同時に更新でき、
     * 二重予約（lost update = 更新の消失）が起きる。
     */
    @Version
    private Long version;

    /**
     * 状態は文字列ではなく enum で保持する。
     * EnumType.STRING にすると、DBには "AVAILABLE" のように名前で保存され、
     * あとで enum に値を追加しても番号ズレの事故が起きない（ORDINALより安全）。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private Long reservedUserId;

    protected Seat() {
        // JPAが内部的に使う。アプリのコードからは new させない。
    }

    /**
     * 「予約する」という業務ルールを、状態を持つ Seat 自身に持たせる（振る舞いをデータの近くに置く）。
     *
     * bad/ では呼び出し側が seat.setStatus("RESERVED") と勝手に状態を書き換えていたため、
     * 「予約可能かどうかのチェック」を書き忘れると簡単に不正な状態遷移ができてしまった。
     * ここに集約しておけば、「AVAILABLE のときしか予約できない」というルールを1か所で守れる。
     */
    public void reserve(Long userId) {
        if (this.status != SeatStatus.AVAILABLE) {
            // すでに他人が押さえた席を上書き予約させない（他人の予約を奪わせない）
            throw new SeatAlreadyReservedException(this.id);
        }
        this.status = SeatStatus.RESERVED;
        this.reservedUserId = userId;
    }
}
