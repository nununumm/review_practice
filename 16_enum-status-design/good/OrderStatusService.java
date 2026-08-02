package com.example.order;

import org.slf4j.Logger;              // ログ出力用のインターフェース（println の代わり）
import org.slf4j.LoggerFactory;      // Logger を作る工場
import org.springframework.stereotype.Service;

/**
 * 注文（Order）のステータスを進めたり、状態に応じた判定を行うサービス。
 * 注文は「新規受付(NEW) → 支払い済み(PAID) → 発送済み(SHIPPED) → 配達完了(DELIVERED)」と進む。
 *
 * 【bad/ からの主な改善】
 *   1. ステータスを文字列 → enum(OrderStatus) にして、綴りミス("PAYED")や表記ゆれを撲滅。
 *   2. 比較は enum の == で行う（enum なら == が正しい。文字列の == バグはこれで消える）。
 *   3. 進めないときは println で素通りせず、例外を投げて処理を止める。
 *   4. 表示ラベルや「キャンセル可否」の判定は状態自身(OrderStatus)に持たせ、if の羅列をやめた。
 *
 * このサービスは自分の中に状態フィールドを持たない「ステートレス」なクラス。
 * 状態は引数の order ごとに別々なので、複数リクエストが同時に来ても混ざらない。
 */
@Service
public class OrderStatusService {

    // このクラス専用のロガー。どのクラスが出したログか自動で付く。
    private static final Logger log = LoggerFactory.getLogger(OrderStatusService.class);

    /**
     * 支払い完了時に呼ばれる。 NEW → PAID に進める。
     */
    public void markAsPaid(Order order) {
        // enum どうしの比較は == でOK（null安全＆コンパイラが型をチェックしてくれる）。
        if (order.getStatus() != OrderStatus.NEW) {
            // 進めない状態なら、例外で明確に知らせて処理を止める（素通りさせない）。
            throw new IllegalOrderTransitionException(
                    "支払い済みに進められません。現在の状態: " + order.getStatus());
        }
        order.setStatus(OrderStatus.PAID);
        // 記録として INFO ログを残す。注文IDのような“業務の追跡に必要な情報”だけを出す
        // （パスワード等の機密は出さない ← 第7・11問の学び）。
        log.info("注文 {} を支払い済みにしました", order.getId());
    }

    /**
     * 出荷担当が発送したときに呼ばれる。 PAID → SHIPPED に進める。
     */
    public void ship(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalOrderTransitionException(
                    "発送できません。現在の状態: " + order.getStatus());
        }
        order.setStatus(OrderStatus.SHIPPED);
        log.info("注文 {} を発送済みにしました", order.getId());
    }

    /**
     * 画面に表示する日本語ラベルを返す。
     * bad/ の長い if-else は不要。状態自身がラベルを知っているので1行で済む。
     * enum は取りうる値が固定なので「不明なステータス」という保険も要らない。
     */
    public String getStatusLabel(Order order) {
        return order.getStatus().getLabel();
    }

    /**
     * キャンセル可能かどうかを判定する。
     * 判定ロジックは状態自身(OrderStatus#isCancellable)に委ねる。
     */
    public boolean canCancel(Order order) {
        return order.getStatus().isCancellable();
    }
}
