package com.example.reservation;

/**
 * 指定されたIDの座席が存在しないときに投げる例外。
 * findById(...).get() の代わりに orElseThrow(...) でこの例外を投げることで、
 * 「その座席は無い」という意図が呼び出し側に伝わる。
 */
public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(Long seatId) {
        super("座席が見つかりません。seatId=" + seatId);
    }
}
