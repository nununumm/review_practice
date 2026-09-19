package com.example.payment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook で受け取った「入金完了」を、注文・ポイントに反映する業務ロジック。
 *
 * 【なぜ Controller から分けるのか（第08問の再来）】
 * Controller は「受け口」に徹し、業務ロジックは Service に置く。
 * こうすると Service 単体でテストしやすく、責務(役割)も明確になる。
 */
@Service
public class PaymentWebhookService {

    // 100円ごとに1ポイント。bad/ の "paidAmount / 100" の 100 は意味不明な数字(マジックナンバー)だった。
    // 名前を付けて「この100は"1ポイントあたりの円"」と意図を明示する。
    private static final int YEN_PER_POINT = 100;

    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final ProcessedWebhookEventRepository processedEventRepository;
    private final MailSender mailSender;

    // コンストラクタインジェクション（依存を外から渡す）。テスト時にモックへ差し替え可能。
    public PaymentWebhookService(OrderRepository orderRepository,
                                 PointRepository pointRepository,
                                 ProcessedWebhookEventRepository processedEventRepository,
                                 MailSender mailSender) {
        this.orderRepository = orderRepository;
        this.pointRepository = pointRepository;
        this.processedEventRepository = processedEventRepository;
        this.mailSender = mailSender;
    }

    /**
     * 入金完了イベントを処理する。
     *
     * @Transactional：このメソッド内のDB更新（注文・ポイント・処理済み記録）を
     *   "全部成功 or 全部取り消し" にまとめる。途中でエラーが出れば全てロールバックされ、
     *   「注文だけPAIDになってポイントは付いていない」といった中途半端な状態を防ぐ（第03問）。
     *
     * @return true=今回このイベントを処理した / false=すでに処理済みだったのでスキップした
     */
    @Transactional
    public boolean handlePaymentSucceeded(PaymentWebhookRequest request) {
        // ── ① 冪等性チェック：このイベントIDは処理済みか？ ──
        // 再送された同じ通知なら、ここで何もせず抜ける（＝二重付与を防ぐ）。
        if (processedEventRepository.existsById(request.eventId())) {
            return false; // すでに処理済み。呼び出し元にはスキップした旨を伝える
        }

        // ── ② 対象注文を取得。無ければ例外（bad/ の .get() 乱用をやめる：第05問） ──
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        // ── ③ 状態ガード：すでにPAIDなら二重処理しない（①と合わせて二重の安全網） ──
        if (order.getStatus() == OrderStatus.PAID) {
            return false;
        }

        // ── ④ 業務処理：注文を支払い済みに ──
        order.setStatus(OrderStatus.PAID);          // 文字列でなく enum で状態を持つ
        order.setPaidAmount(request.amount());
        orderRepository.save(order);

        // ── ⑤ ポイント付与（意味のある定数で計算） ──
        Point point = pointRepository.findByUserId(order.getUserId());
        point.setAmount(point.getAmount() + request.amount() / YEN_PER_POINT);
        pointRepository.save(point);

        // ── ⑥ 「処理済み」を記録。eventId の一意制約が二重処理の最終防壁 ──
        try {
            processedEventRepository.save(new ProcessedWebhookEvent(request.eventId()));
        } catch (DataIntegrityViolationException e) {
            // ①のチェックをすり抜けて"同時"に2件処理された場合でも、
            // 一意制約違反でここに来る。トランザクションはロールバックされ、二重付与は起きない。
            return false;
        }

        // ── ⑦ 確認メール（メール送信は外部連携。トランザクション設計上の注意は後述） ──
        //     宛先は payload の email を信用せず、DB上の注文が持つ正規のアドレスを使う。
        mailSender.send(order.getUserEmail(), "お支払いを確認しました");

        return true;
    }
}
