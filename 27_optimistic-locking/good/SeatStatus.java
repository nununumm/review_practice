package com.example.reservation;

/**
 * 座席の状態を表す enum（列挙型）。
 *
 * bad/ では "AVAILABLE" / "RESERVED" という「ただの文字列」で状態を持っていた。
 * 文字列だと "RESERVE" のようにtypoしてもコンパイルが通ってしまい、
 * バグが実行時まで発覚しない。enum なら「決められた値」以外は書けないので、
 * 存在しない状態を代入する事故が起きない（＝コンパイラが守ってくれる）。
 */
public enum SeatStatus {
    AVAILABLE,   // 空席（予約可能）
    HELD,        // 仮押さえ中（決済待ちなど、一時的に確保している状態）
    RESERVED     // 予約確定
}
