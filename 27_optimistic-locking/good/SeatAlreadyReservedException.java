package com.example.reservation;

/**
 * すでに予約済み（AVAILABLE でない）座席を予約しようとしたときに投げる例外。
 * これを HTTP 409 (Conflict) 等に対応づければ、
 * クライアントに「その席は取られました。別の席をどうぞ」と正しく伝えられる。
 */
public class SeatAlreadyReservedException extends RuntimeException {
    public SeatAlreadyReservedException(Long seatId) {
        super("その座席はすでに予約されています。seatId=" + seatId);
    }
}
