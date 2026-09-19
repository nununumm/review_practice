package com.example.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * 「このWebhookイベントは処理済み」という記録を残すためのエンティティ（＝DBの1テーブルに対応）。
 *
 * 【役割：冪等性(べきとうせい)の要】
 * 冪等性 ＝ 同じ通知が何回届いても、結果は1回分に保たれる性質。
 * 決済プロバイダは 2xx を受け取るまで同じ通知を再送するので、
 * 「一度処理したイベントID」をここに記録し、2回目以降は処理をスキップする。
 *
 * 【ポイント：eventId に "一意制約(ユニーク制約)" を張る】
 * @Column(unique = true) により、DBレベルで「同じ eventId は2行入れられない」を保証する。
 * これが効くのは"同時に"2つの通知が届いたとき。アプリ側の「存在チェック→登録」だけだと
 * すきま(第10問のCheck-Then-Act)で二重登録され得るが、DBの一意制約なら
 * 2つ目の登録は必ず失敗する＝競合しても二重処理を物理的に防げる。
 */
@Entity
public class ProcessedWebhookEvent {

    @Id
    private String eventId;          // プロバイダが振るイベント固有ID。これを主キーにする

    @Column(nullable = false)
    private Instant processedAt;     // いつ処理したか（監査・調査用）

    protected ProcessedWebhookEvent() {
        // JPA（DBとJavaを橋渡しする仕組み）が内部で使うためのデフォルトコンストラクタ
    }

    public ProcessedWebhookEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }
}
