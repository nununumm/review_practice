package com.example.reservation;

/**
 * 空席が1つも無いときに投げる例外。
 * bad/ では target が null のまま NullPointerException になり、原因が分かりにくかった。
 * 「空席が無い」という事実を、名前で意味の分かる例外として表現する。
 */
public class NoAvailableSeatException extends RuntimeException {
    public NoAvailableSeatException(Long eventId) {
        super("空席がありません。eventId=" + eventId);
    }
}
