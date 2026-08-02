package com.example.order;

import org.springframework.stereotype.Service;

/**
 * 注文（Order）のステータスを進めたり、ステータスに応じた処理を振り分けるサービス。
 * 注文は「新規受付 → 支払い済み → 発送済み → 配達完了」と進んでいく。
 * キャンセルされることもある。
 */
@Service
public class OrderStatusService {

    /**
     * 注文の支払いが完了したときに呼ばれる。
     * ステータスを支払い済みに進める。
     */
    public void markAsPaid(Order order) {
        if (order.getStatus().equals("NEW")) {
            order.setStatus("PAYED");
        } else {
            System.out.println("支払いに進められない状態です: " + order.getStatus());
        }
    }

    /**
     * 出荷担当が商品を発送したときに呼ばれる。
     * ステータスを発送済みに進める。
     */
    public void ship(Order order) {
        if (order.getStatus() == "PAID") {
            order.setStatus("SHIPPED");
        } else {
            System.out.println("発送できない状態です: " + order.getStatus());
        }
    }

    /**
     * 画面に表示する日本語ラベルと、送料無料かどうかを返す。
     */
    public String getStatusLabel(Order order) {
        String status = order.getStatus();
        if (status.equals("NEW")) {
            return "ご注文を受け付けました";
        } else if (status.equals("PAID")) {
            return "お支払いを確認しました";
        } else if (status.equals("SHIPPED")) {
            return "発送しました";
        } else if (status.equals("DELIVERED")) {
            return "お届けが完了しました";
        }
        return "不明なステータス";
    }

    /**
     * キャンセル可能かどうかを判定する。
     * まだ発送していなければキャンセルできる。
     */
    public boolean canCancel(Order order) {
        String s = order.getStatus();
        if (s.equals("NEW") || s.equals("PAID")) {
            return true;
        }
        return false;
    }
}
