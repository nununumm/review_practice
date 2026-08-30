package com.example.member.service;

/**
 * 会員の状態を表す列挙型（enum）。
 *
 * bad版は member.setStatus("ACTIVE") のように"生の文字列"で状態を持っていた。
 * これだと "ACTIVE"/"active"/"ACTIV"（打ち間違い）などの表記ゆれをコンパイラが検出できない。
 * enum にすると「取りうる値」が型で保証され、typo は即コンパイルエラーになる。
 */
public enum MemberStatus {
    ACTIVE,     // 有効
    SUSPENDED,  // 一時停止
    WITHDRAWN   // 退会済み
}
