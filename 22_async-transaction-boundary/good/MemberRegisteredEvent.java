package com.example.member.service;

/**
 * 「会員が登録された」という出来事（イベント）を表す不変オブジェクト。
 *
 * サービスはこれを発行するだけで、"誰がどう処理するか"を知らなくてよい（疎結合）。
 * メール送信・ポイント付与など後続処理を増やしても、サービス本体には手を入れず
 * リスナーを足すだけで拡張できる。
 *
 * record を使うと、フィールド・コンストラクタ・getter・equals などを自動生成できる。
 */
public record MemberRegisteredEvent(Long memberId) {
}
