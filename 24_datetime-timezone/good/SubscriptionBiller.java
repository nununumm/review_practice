import java.time.LocalDate;

/**
 * 「1契約ぶんの請求」を担当するクラス。
 * このクラスのメソッド1回 = トランザクション1つ（＝まとめて成功か、まとめて無かったことにする単位）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBiller {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 請求書を作り、次回請求日を進める。
     *
     * @Transactional により、請求書の保存と契約の更新は「両方成功」か「両方なかったこと」になる。
     * これが無いと、請求書だけ保存されて次回請求日が進まず、
     * 翌日のバッチでもう一度請求される（＝二重請求）事故が起きる。
     */
    @Transactional
    public void bill(Long subscriptionId, LocalDate today) {
        // findById は「見つからないかもしれない」ので Optional で返る。
        // .get() は中身が無いと意味不明な例外になるので使わない。
        // orElseThrow で「何が無かったのか」が分かる例外を投げる。
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("契約が存在しません id=" + subscriptionId));

        // 請求日は「今日」ではなく契約が持つ請求予定日を使う。
        // こうすると、バッチが1日遅れて動いても請求日がズレない（＝実行時刻に結果が左右されない）。
        LocalDate billingDate = sub.getNextBillingDate();

        // 【冪等性(べきとうせい)の担保】
        // 冪等性 = 「同じ処理を何回実行しても結果が同じ」という性質。
        // バッチは障害で再実行されることがあるので、すでに同じ請求日の請求書があればスキップする。
        // （DB側にも (subscription_id, billing_date) のユニーク制約を張って二重に守るのが理想）
        if (invoiceRepository.existsBySubscriptionIdAndBillingDate(subscriptionId, billingDate)) {
            log.info("すでに請求済みのためスキップします subscriptionId={} billingDate={}",
                    subscriptionId, billingDate);
            return;
        }

        Invoice invoice = new Invoice();
        invoice.setSubscriptionId(subscriptionId);
        invoice.setAmount(sub.getMonthlyFee());
        // 日付は「文字列」ではなく LocalDate のまま保存する。
        // 文字列にすると並び替え・範囲検索ができず、書式ゆれの温床にもなる。
        invoice.setBillingDate(billingDate);
        invoiceRepository.save(invoice);

        // 次回請求日を1ヶ月進める
        sub.setNextBillingDate(nextBillingDate(billingDate, sub.getBillingAnchorDay()));
        subscriptionRepository.save(sub);

        // メール送信は「コミット後」に行いたいので、ここでは"出来事(イベント)"を通知するだけ。
        // 実際の送信は BillingMailListener が、DBへの確定後に受け取って行う。
        eventPublisher.publishEvent(
                new SubscriptionBilledEvent(subscriptionId, sub.getEmail(), sub.getMonthlyFee(), billingDate));
    }

    /**
     * 次回請求日を計算する。
     *
     * anchorDay（＝契約時に決めた「毎月何日に請求するか」）を基準に月を進める。
     *
     * なぜ「30日を足す」ではダメか:
     *   1月1日 → 1月31日 → 3月2日 … と、月をまたぐたびに請求日が前へズレていく。
     *   1年経つと請求日が5日以上ズレ、「今月2回請求された」というクレームになる。
     *
     * なぜ plusMonths だけでは足りないか:
     *   1月31日 + 1ヶ月 = 2月28日（存在しない日は自動で月末に丸められる）。
     *   ここまでは親切だが、その 2月28日 + 1ヶ月 = 3月28日 となり、
     *   以降ずっと28日請求になってしまう（＝ズレが元に戻らない）。
     *   そこで「毎月31日が基準」という anchorDay を持っておき、
     *   その月に31日が無ければその月の末日にする、という計算にする。
     */
    private LocalDate nextBillingDate(LocalDate current, int anchorDay) {
        LocalDate nextMonth = current.plusMonths(1);
        // lengthOfMonth() = その月が何日まであるか（2月なら28 or 29）
        int day = Math.min(anchorDay, nextMonth.lengthOfMonth());
        return nextMonth.withDayOfMonth(day);
    }
}
