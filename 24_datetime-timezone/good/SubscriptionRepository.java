import java.time.LocalDate;
import java.util.List;

/**
 * 契約(Subscription)を読み書きするリポジトリ。
 *
 * ポイントは findAll() を使わないこと。
 * 「全部取ってきてJava側で絞る」のではなく「必要な分だけDBに取りに行く」。
 * 絞り込みは SQL（＝WHERE句）でやるのが鉄則。
 * Stream API での filter は "取ってきた後" の話なので、DB負荷は減らない。
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 今日が請求日を迎えている契約の「IDだけ」を返す。
     *
     * 条件:
     *   1. nextBillingDate <= 今日     … 請求日が来ている（遅延実行にも耐えるよう「以下」で判定）
     *   2. status = ACTIVE             … 有効な契約だけ
     *   3. cancelDate が未設定、または今日より後 … 解約予定日を過ぎた契約には請求しない
     *
     * エンティティ全体ではなく ID だけを取るのは、
     * 1件ずつ別トランザクションで処理する設計にしているため（メモリも節約できる）。
     */
    @Query("""
            SELECT s.id FROM Subscription s
             WHERE s.nextBillingDate <= :today
               AND s.status = com.example.billing.SubscriptionStatus.ACTIVE
               AND (s.cancelDate IS NULL OR s.cancelDate > :today)
             ORDER BY s.id
            """)
    List<Long> findIdsDueForBilling(@Param("today") LocalDate today, Pageable pageable);
}
