import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 日次請求バッチの「指揮者」。
 *
 * 役割はあえて2つに分けている:
 *   - このクラス   : 対象を集めて、1件ずつ担当者に渡す（トランザクションは持たない）
 *   - SubscriptionBiller : 1契約ぶんの請求処理（こちらが @Transactional を持つ）
 *
 * なぜ分けるのか？
 *   (1) 全件を1つのトランザクションで囲むと、1件の失敗で全件が巻き戻る＆DBが重くなる。
 *       「1契約＝1トランザクション」にすれば、失敗はその契約だけで済む。
 *   (2) Spring の @Transactional は「別のクラスのメソッドを呼んだとき」に効く仕組み（プロキシ）。
 *       同じクラス内で自分のメソッドを呼ぶと効かない（＝第22問でやった落とし穴）。
 */
@Component
@RequiredArgsConstructor
@Slf4j // Lombok: log という名前のロガーを自動で用意してくれる
public class SubscriptionBillingBatch {

    /** 1回のクエリで取得する件数。全件を一気にメモリへ載せないための区切り */
    private static final int CHUNK_SIZE = 500;

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBiller subscriptionBiller;
    private final Clock clock; // 「今」は必ずこの時計から受け取る（new Date() は使わない）

    /**
     * 毎日0時に起動する。
     * zone を明示しているのがポイント。書かないとサーバーのタイムゾーンの0時に動いてしまい、
     * 海外リージョン(UTC)では日本時間の朝9時に動く、という事故になる。
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tokyo")
    public void billDueSubscriptions() {
        // 「今日」を、時計が持つタイムゾーンで確定させる。時刻は持たない LocalDate。
        LocalDate today = LocalDate.now(clock);
        log.info("請求バッチを開始します date={}", today);

        int success = 0;
        int failed = 0;

        // 500件ずつ取り出して処理する（全件を一度にメモリへ載せない）
        List<Long> ids;
        while (!(ids = subscriptionRepository
                .findIdsDueForBilling(today, PageRequest.of(0, CHUNK_SIZE))).isEmpty()) {

            for (Long subscriptionId : ids) {
                try {
                    // 別クラスのメソッドを呼ぶので @Transactional が有効に効く
                    subscriptionBiller.bill(subscriptionId, today);
                    success++;
                } catch (Exception e) {
                    // 1件失敗しても他の契約は処理を続ける。
                    // ただし printStackTrace で握りつぶさず、
                    // 「どの契約が」「なぜ」失敗したかをログに必ず残す（後から追える形にする）。
                    failed++;
                    log.error("請求処理に失敗しました subscriptionId={}", subscriptionId, e);
                }
            }
            // 成功した契約は nextBillingDate が翌月へ進むので、次のループでは対象から外れる。
            // ＝失敗した契約だけが残り続けて無限ループになるのを防ぐため、
            //   実運用では「失敗した契約はスキップ対象に印を付ける」等の考慮も必要。
            if (failed >= ids.size()) {
                log.warn("取得した全件が失敗したため、処理を打ち切ります");
                break;
            }
        }

        log.info("請求バッチを終了します date={} 成功={} 失敗={}", today, success, failed);
    }
}
