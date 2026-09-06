import java.time.Clock;

/**
 * 「今の時刻」を提供する Clock を、DIできる部品（Bean）として登録する設定クラス。
 *
 * なぜわざわざこんなことをするのか？
 *   - new Date() や LocalDate.now() をコード内で直接呼ぶと、
 *     「今日が何日か」をテストからコントロールできない
 *     → 「1月31日の翌月請求日は？」というテストが書けない。
 *   - Clock を注入する形にしておけば、テストでは
 *     Clock.fixed(Instant.parse("2026-01-31T00:00:00Z"), ZoneId.of("Asia/Tokyo"))
 *     のように「時間を止めた時計」を渡せる＝いつ実行しても同じ結果になる。
 *
 * さらに Clock はタイムゾーンも一緒に持つので、
 * 「サーバーのデフォルトTZに黙って依存する」事故も同時に防げる。
 */
@Configuration
@RequiredArgsConstructor
public class ClockConfig {

    private final BillingProperties billingProperties;

    @Bean
    public Clock clock() {
        // 「システムの現在時刻を、請求基準のタイムゾーンで読む時計」を返す
        return Clock.system(billingProperties.getZone());
    }
}
